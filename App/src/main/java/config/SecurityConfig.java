package config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@SuppressWarnings("unused")
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .requestMatchers("/login").permitAll() // Permitir acesso à página de login sem autenticação
                .anyRequest().authenticated() // Requer autenticação para qualquer outra requisição
                .and()
            .formLogin()
                .loginPage("/login") // Página de login personalizada
                .defaultSuccessUrl("/home", true) // Redirecionar para a página inicial após login bem-sucedido
                .permitAll()
                .and()
            .logout()
                .permitAll();
        return http.build();
    }
    

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
