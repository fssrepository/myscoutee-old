package com.raxim.myscoutee.common.config.firebase

import com.raxim.myscoutee.profile.data.document.mongo.Profile
import com.raxim.myscoutee.profile.data.document.mongo.Role
import com.raxim.myscoutee.profile.data.document.mongo.User
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class FirebasePrincipal(val user: User, private val roles: List<Role>) : UserDetails {

    override fun getAuthorities(): List<SimpleGrantedAuthority> {
        return roles.map { role ->
            SimpleGrantedAuthority(role.role)
        }
    }

    override fun getPassword(): String {
        return ""
    }

    override fun getUsername(): String {
        return user.email
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return true
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun isEnabled(): Boolean {
        return true
    }
}