package com.raxim.myscoutee.common.config.firebase;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * UsernamePasswordAuthenticationToken
 */
public class FirebaseAuthenticationToken extends AbstractAuthenticationToken {
    private final Object principal;
    private Object credentials;

    /**
     * This constructor can be safely used by any code that wishes to create a
     * `UsernamePasswordAuthenticationToken`, as the
     * [.isAuthenticated] will return `false`.
     *
     * @param principal   the principal to be authenticated
     * @param credentials the credentials to be authenticated
     */
    public FirebaseAuthenticationToken(Object principal, Object credentials) {
        super(null);
        this.principal = principal;
        this.credentials = credentials;
        setAuthenticated(false);
    }

    /**
     * This constructor should only be used by
     * `AuthenticationManager` or `AuthenticationProvider`
     * implementations that are satisfied with producing a trusted (i.e.
     * [.isAuthenticated] = `true`) authentication token.
     *
     * @param principal    the principal to be authenticated
     * @param credentials  the credentials to be authenticated
     * @param authorities the authorities granted to the authenticated principal
     */
    public FirebaseAuthenticationToken(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.credentials = credentials;
        super.setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) {
        if (isAuthenticated) {
            throw new IllegalArgumentException("Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
        }
        super.setAuthenticated(false);
    }

    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
        credentials = null;
    }
}
