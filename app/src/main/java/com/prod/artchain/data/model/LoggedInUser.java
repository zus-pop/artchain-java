package com.prod.artchain.data.model;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LoggedInUser {

    private String userId;
    private String fullName;
    private String email;
    private String phone;
    private Date birthday;
    private String ward;
    private String grade;
    private UserRole role;
    private String accessToken;
}