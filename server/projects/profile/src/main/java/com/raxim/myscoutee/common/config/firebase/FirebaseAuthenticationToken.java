package com.raxim.myscoutee.common.config.firebase

import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority


/**
 * UsernamePasswordAuthenticationToken
 *
 * @author prvoslav
 */
class FirebaseAuthenticationToken : AbstractAuthenticationToken {
    private val principal: Any
    private var credentials: Any?

    /**
     * This constructor can be safely used by any code that wishes to create a
     * `UsernamePasswordAuthenticationToken`, as the
     * [.isAuthenticated] will return `false`.
     *
     */
    constructor(principal: Any, credentials: Any) : super(null) {
        this.principal = principal
        this.credentials = credentials
        isAuthenticated = false
    }

    /**
     * This constructor should only be used by
     * `AuthenticationManager` or `AuthenticationProvider`
     * implementations that are satisfied with producing a trusted (i.e.
     * [.isAuthenticated] = `true`) authentication token.
     *
     * @param principal
     * @param credentials
     * @param authorities
     */
    constructor(
        principal: Any, credentials: Any,
        authorities: Collection<GrantedAuthority?>?
    ) : super(authorities) {
        this.principal = principal
        this.credentials = credentials
        super.setAuthenticated(true) // must use super, as we override
    }

    // ~ Methods
    // ========================================================================================================
    override fun getCredentials(): Any? {
        return credentials
    }

    override fun getPrincipal(): Any {
        return principal
    }

    @Throws(IllegalArgumentException::class)
    override fun setAuthenticated(isAuthenticated: Boolean) {
        require(!isAuthenticated) { "Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead" }
        super.setAuthenticated(false)
    }

    override fun eraseCredentials() {
        super.eraseCredentials()
        credentials = null
    }

    companion object {
        private const val serialVersionUID = -1869548136546750302L
    }
}