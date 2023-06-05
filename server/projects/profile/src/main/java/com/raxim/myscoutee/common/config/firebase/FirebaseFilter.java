package com.raxim.myscoutee.common.config.firebase;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FirebaseFilter extends OncePerRequestFilter {
    private final FirebaseService firebaseService;
    private final Set<String> authSet = new HashSet<>();

    public FirebaseFilter(FirebaseService firebaseService) {
        this.firebaseService = firebaseService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String xAuth = request.getHeader(AUTH_FIREBASE);
        if (xAuth == null || xAuth.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        } else {
            try {
                if (!authSet.contains(xAuth)) {
                    authSet.add(xAuth);

                    FirebaseTokenHolder holder = firebaseService.parseToken(xAuth);
                    String xLink = request.getHeader(AUTH_LINK);

                    UserDetails userDetails = firebaseService.loadUserByUsername(holder.getEmail(), xLink);
                    Authentication auth = new FirebaseAuthenticationToken(userDetails, holder,
                            userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(auth);

                    authSet.remove(xAuth);
                }
                filterChain.doFilter(request, response);
            } catch (FirebaseTokenInvalidException e) {
                throw new SecurityException(e);
            }
        }
    }

    private static final String AUTH_FIREBASE = "X-Authorization-Firebase";
    private static final String AUTH_LINK = "X-Authorization-Link";
}
