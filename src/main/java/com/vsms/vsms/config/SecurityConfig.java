package com.vsms.vsms.config;
// define and register beans(obejcts) to spring container
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests( auth -> auth
                		.requestMatchers("/**").permitAll()
                        .requestMatchers("/success").permitAll()
                        .requestMatchers("/rentalSearch").permitAll()
                        .requestMatchers("/vehicleList").permitAll()
                        .requestMatchers("/ownerType").permitAll()
                        .requestMatchers("images/**").permitAll()
                        .requestMatchers("/ownerRegister").permitAll()
                        .requestMatchers("/vehicleRegister").permitAll()
                        .requestMatchers("css/**").permitAll()
                        .requestMatchers("js/**").permitAll()   
                        .requestMatchers("/navbarRV").permitAll()
                        .requestMatchers("/login").permitAll()
                		.requestMatchers("/contact").permitAll()
	                    .requestMatchers("/userProfile").permitAll()
                        .requestMatchers("/userEdit").permitAll()
	                    .requestMatchers("/userRegister").permitAll()
	                    .requestMatchers("/userLogin").permitAll()
	                    .requestMatchers("/logout").permitAll()
	                    .requestMatchers("/renterHome/**").hasRole("client")
	                    .requestMatchers("/admin/**").hasRole("admin")
	                    .anyRequest().authenticated()
                )
                .formLogin(form -> form
                		.loginPage("/login")
                		.usernameParameter("email")
                		.passwordParameter("password")
                		.defaultSuccessUrl("/", true)
                )
                .logout(config -> config.logoutSuccessUrl("/"))
        		.build();
    }
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
}
