package com.acme.portal.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

/**
 * Security setup of the portal: our provider, JSON 401/403, session login, URL rules and method security.
 * The AuthenticationManager bean below is what AbstractSecuredController signs users in with.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class LegacySecurityConfigurer extends WebSecurityConfigurerAdapter {

    private final PortalAuthenticationProvider provider;
    private final RestAuthenticationEntryPoint entryPoint;
    private final RestAccessDeniedHandler deniedHandler;
    private final SecurityContextBridge bridge;

    public LegacySecurityConfigurer(PortalAuthenticationProvider provider, RestAuthenticationEntryPoint entryPoint, RestAccessDeniedHandler deniedHandler, SecurityContextBridge bridge) {
        this.provider = provider;
        this.entryPoint = entryPoint;
        this.deniedHandler = deniedHandler;
        this.bridge = bridge;
    }

    /*~~(Migrate manually based on https://spring.io/blog/2022/02/21/spring-security-without-the-websecurityconfigureradapter)~~>*/@Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(provider);
    }

    @Override
    protected void configure(HttpSecurity http) {
        http.csrf(csrf -> csrf.disable())
            .sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .exceptionHandling(handling -> handling.authenticationEntryPoint(entryPoint).accessDeniedHandler(deniedHandler))
            .logout(logout -> logout.logoutUrl("/api/logout")
                .addLogoutHandler((request, response, authentication) -> bridge.clear(request))
                .logoutSuccessHandler((request, response, authentication) -> response.setStatus(HttpServletResponse.SC_NO_CONTENT)))
            .authorizeHttpRequests(requests -> requests
                .requestMatchers("/api/health", "/api/logout").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/session").permitAll()
                .requestMatchers("/api/admin/**").hasRole(Roles.ADMIN)
                .requestMatchers("/api/**").authenticated()
                .anyRequest().permitAll());
    }

    /*~~(Migrate manually based on https://spring.io/blog/2022/02/21/spring-security-without-the-websecurityconfigureradapter)~~>*/@Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}
