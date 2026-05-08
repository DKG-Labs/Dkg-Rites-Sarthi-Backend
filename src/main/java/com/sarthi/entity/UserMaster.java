package com.sarthi.entity;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

@Entity
@Table(name = "USER_MASTER")
@Data
public class UserMaster implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USERID")
    private Integer userId;

    @Column(name = "USERNAME")
    private String userName;
    @Column(name = "role_name")
    private String roleName;

    private String employeeId;

    @Column(name = "PASSWORD")
    private String password;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "MOBILENUMBER")
    private String mobileNumber;

    @Column(name = "EMPLOYEE_CODE", length = 50)
    private String employeeCode;

    @Column(name = "RITES_EMPLOYEE_CODE")
    private Integer ritesEmployeeCode;

    @Column(name = "EMPLOYMENT_TYPE", length = 20)
    private String employmentType; // REGULAR / CONTRACTUAL / MPA

    @Column(name = "FULL_NAME", length = 255)
    private String fullName;

    @Column(name = "SHORT_NAME", length = 100, unique = true)
    private String shortName;

    @Column(name = "DATE_OF_BIRTH")
    private String dateOfBirth;

    @Column(name = "DESIGNATION", length = 100)
    private String designation;

    @Column(name = "DISCIPLINE", length = 50)
    private String discipline;

    @Column(name = "CREATEDBY")
    private String createdBy;

    private String productType;

    @Column(name = "Region", length = 50)
    private String rio;

    @Column(name = "CREATEDDATE")
    private LocalDateTime createdDate = LocalDateTime.now();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        if (roleName == null || roleName.trim().isEmpty()) {
            return Collections.singletonList(new SimpleGrantedAuthority("USER"));
        }

        // Convert comma-separated roles → authorities
        return Arrays.stream(roleName.split(","))
                .map(String::trim)
                .map(SimpleGrantedAuthority::new)
                .toList(); // This is OK in Java 17+
    }

    @Override
    public String getUsername() {
        return this.userName;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
