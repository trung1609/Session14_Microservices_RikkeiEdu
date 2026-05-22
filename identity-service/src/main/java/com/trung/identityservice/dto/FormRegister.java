package com.trung.identityservice.dto;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class FormRegister {
    private String username;
    private String password;
    private String role;
    private List<String> permissions;
}
