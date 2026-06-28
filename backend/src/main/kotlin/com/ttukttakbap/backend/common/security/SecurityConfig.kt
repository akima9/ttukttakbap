package com.ttukttakbap.backend.common.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain

// 어드민(/api/v1/admin/**)만 ROLE_ADMIN(HTTP Basic)으로 보호하고, 소비자 API는 공개한다.
// 계정은 고정 어드민 1개. 운영에서는 ADMIN_USERNAME/ADMIN_PASSWORD 환경변수로 오버라이드한다.
@Configuration
class SecurityConfig(
    @Value("\${admin.username:admin}") private val adminUsername: String,
    @Value("\${admin.password:admin1234}") private val adminPassword: String,
) {
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun userDetailsService(passwordEncoder: PasswordEncoder): UserDetailsService =
        InMemoryUserDetailsManager(
            User.withUsername(adminUsername)
                .password(passwordEncoder.encode(adminPassword))
                .roles("ADMIN")
                .build(),
        )

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain =
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                it.requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                    .anyRequest().permitAll()
            }
            .httpBasic {}
            .build()
}
