package com.bank.los.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    private Long   id;
    private String email;
    private String userCode;      // EmpNo — business-facing employee number
    private String fullName;
    private String userType;      // INTERNAL / STAFF / CUSTOMER
    private String role;          // INTERNAL_ADMIN / ADMIN / MAKER / CHECKER / VIEWER / CUSTOMER
    private String organizationCode;
    private String tenantDbName;
    private Long   branchId;

    /** JWT ID — used for session activity tracking and auto-logout */
    private String jti;

    @JsonIgnore
    private String password;

    private boolean active;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String roleWithPrefix = (role != null && role.startsWith("ROLE_")) ? role : "ROLE_" + role;
        return Collections.singletonList(new SimpleGrantedAuthority(roleWithPrefix));
    }

    @Override
    public String getPassword() { return password; }

    @Override
    public String getUsername() { return email != null ? email : userCode; }

    @Override public boolean isAccountNonExpired()    { return true; }
    @Override public boolean isAccountNonLocked()     { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()              { return active; }
}
