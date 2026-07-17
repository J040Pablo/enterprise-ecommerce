package com.joaopablo.ecommerce.auth.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String cpf;
    private String phone;
    private Boolean enabled;
    private Boolean emailVerified;

}