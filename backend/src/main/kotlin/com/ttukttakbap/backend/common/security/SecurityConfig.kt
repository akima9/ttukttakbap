package com.ttukttakbap.backend.common.security

import com.ttukttakbap.backend.auth.jwt.JwtAuthenticationFilter
import com.ttukttakbap.backend.auth.jwt.JwtProvider
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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

// 어드민(/api/v1/admin/**)은 ROLE_ADMIN(HTTP Basic), 소비자 API는 공개.
// 사용자 개인화 API(/api/v1/auth/me, /api/v1/me/**)는 JWT 액세스 토큰으로 인증한다.
// JwtProvider/JwtAuthenticationFilter는 여기서 @Bean으로 등록해, WebMvcTest가 SecurityConfig만 import하면 함께 로드되도록 한다.
@Configuration
class SecurityConfig(
    @Value("\${admin.username:admin}") private val adminUsername: String,
    @Value("\${admin.password:admin1234}") private val adminPassword: String,
    @Value("\${jwt.secret:local-dev-secret-key-please-override-32bytes!!}") private val jwtSecret: String,
    @Value("\${jwt.access-expiry-millis:1800000}") private val accessExpiryMillis: Long,
    @Value("\${jwt.refresh-expiry-millis:1209600000}") private val refreshExpiryMillis: Long,
    @Value("\${cors.allowed-origins:http://localhost:3000}") private val allowedOrigins: String,
) {
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    // 프론트(브라우저)에서 다른 오리진의 API를 호출하므로 CORS 허용. 오리진은 env로 조정 가능.
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val config = CorsConfiguration().apply {
            allowedOrigins = this@SecurityConfig.allowedOrigins.split(",").map { it.trim() }
            allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
            allowedHeaders = listOf("*")
        }
        return UrlBasedCorsConfigurationSource().apply { registerCorsConfiguration("/**", config) }
    }

    @Bean
    fun userDetailsService(passwordEncoder: PasswordEncoder): UserDetailsService =
        InMemoryUserDetailsManager(
            User.withUsername(adminUsername)
                .password(passwordEncoder.encode(adminPassword))
                .roles("ADMIN")
                .build(),
        )

    @Bean
    fun jwtProvider(): JwtProvider = JwtProvider(jwtSecret, accessExpiryMillis, refreshExpiryMillis)

    @Bean
    fun jwtAuthenticationFilter(jwtProvider: JwtProvider): JwtAuthenticationFilter =
        JwtAuthenticationFilter(jwtProvider)

    @Bean
    fun securityFilterChain(http: HttpSecurity, jwtAuthenticationFilter: JwtAuthenticationFilter): SecurityFilterChain =
        http
            .cors {}
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                it.requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                    .requestMatchers("/api/v1/auth/me").authenticated()
                    .requestMatchers("/api/v1/me/**").authenticated()
                    .anyRequest().permitAll()
            }
            .httpBasic {}
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()
}
