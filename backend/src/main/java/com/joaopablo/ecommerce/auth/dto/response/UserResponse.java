package com.joaopablo.ecommerce.auth.dto.response;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String cpf;
    private String phone;
    private Boolean enabled;
    private Boolean emailVerified;

}