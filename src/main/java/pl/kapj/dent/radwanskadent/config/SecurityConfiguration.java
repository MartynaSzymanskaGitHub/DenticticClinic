package pl.kapj.dent.radwanskadent.config;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import pl.kapj.dent.radwanskadent.model.Role;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfiguration {

    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfiguration(UserDetailsService userDetailsService,
                                 JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
            .authenticationProvider(authProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(authz -> authz
                .dispatcherTypeMatchers(DispatcherType.FORWARD).permitAll()

                .requestMatchers("/", "/home", "/login", "/favicon.ico", "/static/**", "/resources/**").permitAll()
                .requestMatchers("/user/register", "/user/emailConfirmation", "/user/activate").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/user/profile", "/user/profile/**").authenticated()

                .requestMatchers("/admin/**").hasRole(Role.ADMIN.name())
                .requestMatchers("/dentist/**").hasRole(Role.DENTIST.name())
                .requestMatchers("/api/stats/**").hasRole(Role.ADMIN.name())
                .requestMatchers("/client/**", "/appointment/**").hasRole(Role.CLIENT.name())
                .requestMatchers("/invoice/**").hasAnyRole(Role.ADMIN.name(), Role.CLIENT.name())
                .requestMatchers("/recipe/**").hasAnyRole(Role.ADMIN.name(), Role.CLIENT.name(), Role.DENTIST.name())

                .anyRequest().authenticated()
            )
            .logout(logout -> logout
                .deleteCookies(JwtAuthenticationFilter.JWT_COOKIE_NAME, "JSESSIONID")
                .logoutSuccessUrl("/")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login")));

        return http.build();
    }
}