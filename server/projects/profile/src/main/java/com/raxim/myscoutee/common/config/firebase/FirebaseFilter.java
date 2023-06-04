package com.raxim.myscoutee.common.config.firebase

import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class FirebaseFilter(private val firebaseService: FirebaseService) : OncePerRequestFilter() {
    private val authSet = mutableSetOf<String>()

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val xAuth = request.getHeader(AUTH_FIREBASE)
        if (xAuth == null || xAuth.isBlank()) {
            filterChain.doFilter(request, response)
            return
        } else {
            try {
                if (!authSet.contains(xAuth)) {
                    authSet.add(xAuth)

                    val holder: FirebaseTokenHolder = firebaseService.parseToken(xAuth)
                    val xLink = request.getHeader(AUTH_LINK)

                    val userDetails = firebaseService.loadUserByUsername(holder.email, xLink)
                    val auth: Authentication = FirebaseAuthenticationToken(userDetails, holder, userDetails.authorities)
                    SecurityContextHolder.getContext().authentication = auth

                    authSet.remove(xAuth)
                }
                filterChain.doFilter(request, response)
            } catch (e: FirebaseTokenInvalidException) {
                throw SecurityException(e)
            }
        }
    }

    companion object {
        private const val AUTH_FIREBASE = "X-Authorization-Firebase"
        private const val AUTH_LINK = "X-Authorization-Link"
    }
}