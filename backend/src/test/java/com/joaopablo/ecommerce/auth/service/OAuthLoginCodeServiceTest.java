package com.joaopablo.ecommerce.auth.service;

import com.joaopablo.ecommerce.auth.dto.internal.OAuthLoginCodePayload;
import com.joaopablo.ecommerce.auth.dto.response.LoginResponseDTO;
import com.joaopablo.ecommerce.auth.exception.InvalidOAuthLoginCodeException;
import com.joaopablo.ecommerce.auth.repository.OAuthLoginCodeRedisRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuthLoginCodeServiceTest {

    @Mock
    private OAuthLoginCodeRedisRepository repository;

    @InjectMocks
    private OAuthLoginCodeService service;

    private LoginResponseDTO loginResponse;
    private UUID userId;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "ttlSeconds", 90L);
        userId = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11");
        loginResponse = LoginResponseDTO.builder()
                .token("access-jwt")
                .refreshToken("refresh-uuid")
                .type("Bearer")
                .expiresIn(86400000)
                .user(LoginResponseDTO.UserLoginResponse.builder()
                        .id(userId)
                        .firstName("Ana")
                        .lastName("Silva")
                        .email("ana@email.com")
                        .roles(List.of("CUSTOMER"))
                        .build())
                .build();
    }

    @Test
    void issue_shouldStoreOpaqueCodeWithTtlAndNotEmbedTokensInCode() {
        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<OAuthLoginCodePayload> payloadCaptor =
                ArgumentCaptor.forClass(OAuthLoginCodePayload.class);

        String code = service.issue(loginResponse);

        assertNotNull(code);
        assertFalse(code.isBlank());
        assertFalse(code.contains("access-jwt"));
        assertFalse(code.contains("refresh-uuid"));
        assertFalse(code.contains(userId.toString()));

        verify(repository).save(codeCaptor.capture(), payloadCaptor.capture(), eq(Duration.ofSeconds(90)));
        assertEquals(code, codeCaptor.getValue());
        assertEquals("access-jwt", payloadCaptor.getValue().getAccessToken());
        assertEquals("refresh-uuid", payloadCaptor.getValue().getRefreshToken());
        assertEquals(userId, payloadCaptor.getValue().getUserId());
        assertEquals(List.of("CUSTOMER"), payloadCaptor.getValue().getRoles());
    }

    @Test
    void exchange_validCode_returnsTokensOnce() {
        ConcurrentHashMap<String, OAuthLoginCodePayload> store = new ConcurrentHashMap<>();
        doAnswer(inv -> {
            store.put(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(repository).save(anyString(), any(), any());
        when(repository.consume(anyString())).thenAnswer(inv -> store.remove(inv.getArgument(0)));

        String code = service.issue(loginResponse);
        LoginResponseDTO first = service.exchange(code);

        assertEquals("access-jwt", first.getToken());
        assertEquals("refresh-uuid", first.getRefreshToken());
        assertEquals(userId, first.getUser().getId());
        assertEquals(List.of("CUSTOMER"), first.getUser().getRoles());

        assertThrows(InvalidOAuthLoginCodeException.class, () -> service.exchange(code));
    }

    @Test
    void exchange_missingCode_fails() {
        when(repository.consume("nope")).thenReturn(null);
        assertThrows(InvalidOAuthLoginCodeException.class, () -> service.exchange("nope"));
    }

    @Test
    void exchange_blankCode_fails() {
        assertThrows(InvalidOAuthLoginCodeException.class, () -> service.exchange("  "));
        verify(repository, never()).consume(anyString());
    }

    @Test
    void exchange_expiredOrConsumed_fails() {
        when(repository.consume("expired")).thenReturn(null);
        InvalidOAuthLoginCodeException ex = assertThrows(
                InvalidOAuthLoginCodeException.class,
                () -> service.exchange("expired")
        );
        assertTrue(ex.getMessage().toLowerCase().contains("invalid")
                || ex.getMessage().toLowerCase().contains("expired"));
    }

    @Test
    void issue_adminRolesArePreservedInPayload() {
        loginResponse.getUser().setRoles(List.of("ADMIN"));
        ArgumentCaptor<OAuthLoginCodePayload> payloadCaptor =
                ArgumentCaptor.forClass(OAuthLoginCodePayload.class);

        service.issue(loginResponse);

        verify(repository).save(anyString(), payloadCaptor.capture(), any());
        assertEquals(List.of("ADMIN"), payloadCaptor.getValue().getRoles());
    }
}
