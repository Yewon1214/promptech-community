package kr.co.community.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.WebSecurityConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private final SecurityHandler securityHandler;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                //.antMatchers("/posts/new/**", "/posts/**/edit").authenticated()
                .anyRequest().permitAll()
                .and()
                .csrf()
                .and()
                .formLogin()
                .loginPage("/member/login")
                .defaultSuccessUrl("/")
                .failureUrl("/member/login?error=1")
                .permitAll()
                .and()
                .logout()
                .logoutUrl("/member/logout")
                .logoutSuccessUrl("/")
                .permitAll();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
