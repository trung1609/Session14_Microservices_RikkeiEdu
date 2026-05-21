package com.trung.identityservice.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class FormLogin {
    private String username;
    private String password;
}
