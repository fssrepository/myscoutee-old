package com.raxim.myscoutee.common.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.data.repository.query.SecurityEvaluationContextExtension;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.raxim.myscoutee.common.config.firebase.FirebaseAuthenticationProvider;
import com.raxim.myscoutee.common.config.firebase.FirebaseFilter;
import com.raxim.myscoutee.common.config.firebase.FirebaseService;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    private final FirebaseService firebaseService;
    private final FirebaseAuthenticationProvider firebaseProvider;

    public WebSecurityConfig(FirebaseService firebaseService, FirebaseAuthenticationProvider firebaseProvider) {
        this.firebaseService = firebaseService;
        this.firebaseProvider = firebaseProvider;
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web
                .ignoring()
                .requestMatchers("/css/**", "/js/**", "/img/**", "/lib/**", "/favicon.ico");
    }

    @Autowired
    protected void registerProvider(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(firebaseProvider);
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        /*http.addFilterBefore(tokenAuthorizationFilter(), BasicAuthenticationFilter.class)
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests.anyRequest().authenticated())
                .httpBasic(httpBasic -> httpBasic.disable())
                .csrf(csrf -> csrf.disable());*/
        return http.build();
    }

    private FirebaseFilter tokenAuthorizationFilter() {
        return new FirebaseFilter(firebaseService);
    }

    @Bean
    SecurityEvaluationContextExtension securityEvaluationContextExtension() {
        return new SecurityEvaluationContextExtension();
    }
}
