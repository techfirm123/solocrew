
package com.solocrew.platform.identity;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public final class OwnerUserPrincipal
    implements UserDetails, CredentialsContainer, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final UUID userId;
    private final String email;
    private transient String passwordHash;
    private final UserAccountStatus accountStatus;

    public OwnerUserPrincipal(
        UUID userId,
        String email,
        String passwordHash,
        UserAccountStatus accountStatus
    ) {
        this.userId = userId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.accountStatus = accountStatus;
    }

    public UUID userId() {
        return userId;
    }

    public UserAccountStatus accountStatus() {
        return accountStatus;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountStatus != UserAccountStatus.DISABLED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return accountStatus == UserAccountStatus.ACTIVE;
    }

    @Override
    public void eraseCredentials() {
        passwordHash = null;
    }
}
