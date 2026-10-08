package com.bank.los.security;

import com.bank.los.common.constant.ApplicationConstants;
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
    private Long   organizationId; // tenant_id in Master DB
    private java.util.UUID organizationUuid;
    private String organizationCode;
    private String organizationDbName;
    private Long   branchId;

    private String designation;
    private java.util.List<String> permissions;

    /** JWT ID — used for session activity tracking and auto-logout */
    private String jti;

    @JsonIgnore
    private String password;

    private boolean active;

    public boolean hasPermission(String permissionCode) {
        if (permissionCode == null) return false;
        if (ApplicationConstants.Roles.INTERNAL_ADMIN.equalsIgnoreCase(role) ||
            ApplicationConstants.Roles.SUPER_ADMIN.equalsIgnoreCase(role)) {
            return true;
        }
        return permissions != null && permissions.contains(permissionCode);
    }

    /**
     * Checks if this principal is a global platform master administrator.
     * Bank-level Super Admins and Staff are tenant users and return false.
     */
    public boolean isPlatformAdmin() {
        return ApplicationConstants.UserTypes.INTERNAL.equalsIgnoreCase(userType) ||
               (ApplicationConstants.Roles.INTERNAL_ADMIN.equalsIgnoreCase(role) &&
                (organizationId == null || organizationId == 0L || "MASTER".equalsIgnoreCase(organizationCode)));
    }

    /**
     * Validates whether this principal is permitted to access the given organization ID.
     * Platform master administrators have system-wide access.
     */
    public boolean belongsToOrganization(Long targetOrgId) {
        if (targetOrgId == null) return false;
        if (isPlatformAdmin()) return true;
        return targetOrgId.equals(this.organizationId);
    }

    /**
     * Validates whether this principal is permitted to access the given organization identifier (ID, UUID, or Code).
     */
    public boolean belongsToOrganization(String targetOrgIdentifier) {
        if (targetOrgIdentifier == null || targetOrgIdentifier.isBlank()) return false;
        if (isPlatformAdmin()) return true;

        String trimmed = targetOrgIdentifier.trim();
        if (this.organizationId != null && trimmed.equals(String.valueOf(this.organizationId))) {
            return true;
        }
        if (this.organizationCode != null && this.organizationCode.equalsIgnoreCase(trimmed)) {
            return true;
        }
        if (this.organizationUuid != null && this.organizationUuid.toString().equalsIgnoreCase(trimmed)) {
            return true;
        }
        return false;
    }

    // Backward-compatible alias for tenantId
    public Long getTenantId() {
        return organizationId;
    }

    public void setTenantId(Long tenantId) {
        this.organizationId = tenantId;
    }

    // Backward-compatible alias for tenantDbName
    public String getTenantDbName() {
        return organizationDbName;
    }

    public void setTenantDbName(String tenantDbName) {
        this.organizationDbName = tenantDbName;
    }

    public static class UserPrincipalBuilder {
        private String organizationDbName;
        private Long   organizationId;

        public UserPrincipalBuilder tenantId(Long tenantId) {
            this.organizationId = tenantId;
            return this;
        }

        public UserPrincipalBuilder tenantDbName(String tenantDbName) {
            this.organizationDbName = tenantDbName;
            return this;
        }

        public UserPrincipalBuilder organizationDbName(String organizationDbName) {
            this.organizationDbName = organizationDbName;
            return this;
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        java.util.List<GrantedAuthority> authorities = new java.util.ArrayList<>();
        if (role != null) {
            String roleWithPrefix = role.startsWith("ROLE_") ? role : "ROLE_" + role;
            authorities.add(new SimpleGrantedAuthority(roleWithPrefix));
        }
        if (permissions != null) {
            for (String perm : permissions) {
                if (perm != null && !perm.isBlank()) {
                    authorities.add(new SimpleGrantedAuthority(perm));
                }
            }
        }
        return authorities;
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

