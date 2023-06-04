package com.raxim.myscoutee.common.config

import com.raxim.myscoutee.common.config.firebase.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.data.repository.query.SecurityEvaluationContextExtension
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter

@Configuration
@EnableWebSecurity
class WebSecurityConfig(
    private val firebaseService: FirebaseService,
    private val firebaseProvider: FirebaseAuthenticationProvider
) :
    WebSecurityConfigurerAdapter() {
    @Throws(Exception::class)
    override fun configure(web: WebSecurity) {
        web.ignoring().antMatchers("/favicon.ico", "/messages", "/calc/**")
    }

    @Throws(java.lang.Exception::class)
    override fun configure(auth: AuthenticationManagerBuilder) {
        auth.authenticationProvider(firebaseProvider)
    }

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http.addFilterAt(tokenAuthorizationFilter(), BasicAuthenticationFilter::class.java).authorizeRequests()
            .anyRequest().authenticated()
        http.httpBasic().disable().csrf().disable()
    }

    private fun tokenAuthorizationFilter(): FirebaseFilter? {
        return FirebaseFilter(firebaseService)
    }

    @Bean
    fun securityEvaluationContextExtension(): SecurityEvaluationContextExtension? {
        return SecurityEvaluationContextExtension()
    }
}