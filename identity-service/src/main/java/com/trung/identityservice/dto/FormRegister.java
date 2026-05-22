package com.trung.identityservice.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class FormRegister {
    private String username;
    private String password;
    private String role;
}
