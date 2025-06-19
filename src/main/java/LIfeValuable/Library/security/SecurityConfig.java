package LifeValuable.Library.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .securityMatcher("/api/**")
                .authorizeHttpRequests(
                    auth -> auth
                            // открытые эндпоинты
                            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                            .requestMatchers("/api/auth/login", "/api/auth/logout").permitAll()
                            .requestMatchers(HttpMethod.POST, "/api/readers").permitAll()

                            // конкретные эндпоинты
                            .requestMatchers("/api/readers/by-phone", "/api/readers/by-email").hasAnyRole("LIBRARIAN", "ADMIN")
                            .requestMatchers("/api/readers/me").hasAnyRole("READER", "LIBRARIAN", "ADMIN")

                            // эндпоинты с wildcards
                            .requestMatchers(HttpMethod.POST, "/api/books/**").hasRole("ADMIN")
                            .requestMatchers(HttpMethod.PUT, "/api/books/**").hasRole("ADMIN")
                            .requestMatchers(HttpMethod.DELETE, "/api/books/**").hasRole("ADMIN")

                            .requestMatchers("/api/lendings/**").hasAnyRole("LIBRARIAN", "ADMIN")
                            .requestMatchers(HttpMethod.PATCH, "/api/books/*/stock").hasAnyRole("LIBRARIAN", "ADMIN")
                            .requestMatchers(HttpMethod.GET, "/api/readers").hasAnyRole("LIBRARIAN", "ADMIN")
                            .requestMatchers(HttpMethod.GET, "/api/readers/**").hasAnyRole("LIBRARIAN", "ADMIN")
                            .requestMatchers(HttpMethod.PUT, "/api/readers/**").hasAnyRole("LIBRARIAN", "ADMIN")
                            .requestMatchers(HttpMethod.DELETE, "/api/readers/**").hasAnyRole("LIBRARIAN", "ADMIN")


                            .requestMatchers(HttpMethod.GET, "/api/books/**").hasAnyRole("READER", "LIBRARIAN", "ADMIN")


                            .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf.disable())
                .build();
    }
}
