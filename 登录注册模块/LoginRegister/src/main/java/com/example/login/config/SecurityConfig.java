package com.example.login.config;



import com.example.login.jwt.JwtAuthEntryPoint;
import com.example.login.jwt.JwtFilter;
import com.example.login.service.Impl.UserServiceImpl;
import com.example.login.util.RandomCodeUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;



import javax.sql.DataSource;
import java.security.SecureRandom;

@Configuration
@EnableMethodSecurity
@EnableWebSecurity
public class SecurityConfig {


    private final UserDetailsService userDetailsService;
    private final DataSource dataSource;
    private JwtAuthEntryPoint jwtAuthEntryPoint;



    @Lazy
    @Autowired
    public SecurityConfig(UserDetailsService userDetailsService, DataSource dataSource,JwtAuthEntryPoint jwtAuthEntryPoint) {
     this.userDetailsService = userDetailsService;
     this.dataSource  = dataSource;
     this.jwtAuthEntryPoint = jwtAuthEntryPoint;
    }



    /**
     * 配置 remember-me 功能
     * Spring Security 默认使用名为 persistent_logins 的表来存储 Remember Me 的 token 信息
     */
    @Bean
    public PersistentTokenRepository rememberMeTokenRepository() {
        JdbcTokenRepositoryImpl repository = new JdbcTokenRepositoryImpl();
        repository.setDataSource(dataSource);
        return repository;
    }


    /**
     * 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(14, new SecureRandom());
    }



    /**
     * 配置 AuthenticationProvider，用于处理用户认证
     * Spring Security检测到手动配置了AuthenticationProvider，导致默认的UserDetailsService自动配置失效，从而发出警告。
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService); // 修复点：直接设置 UserDetailsService 实例
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * 创建 AuthenticationManager，用于处理用户认证
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }


    /**
     * 创建 SecurityFilterChain，用于处理用户认证和授权
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, UserServiceImpl userServiceImpl) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
             //   .csrf(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login","/api/auth/login","/api/auth/register", "/api/auth/reset-password", "/api/verify/send-code-email", "/api/verify/send-code-phone").permitAll()
                        .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthEntryPoint))
                .addFilterBefore(new JwtFilter(), UsernamePasswordAuthenticationFilter.class)



                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.getWriter().write("{\"message\": \"登出成功\"}");
                        })
                        .deleteCookies("JSESSIONID")
                        .invalidateHttpSession(true)
                        .permitAll()
                )


                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .rememberMe(remember -> remember
                        .rememberMeCookieName("remember-me")
                        .tokenRepository(rememberMeTokenRepository()) // 使用数据库存储
                        .key(RandomCodeUtil.generateAlphaNumericKey(20)) // 可选：自定义 key，默认使用安全随机生成的 key
                        .tokenValiditySeconds(15)  // 设置 token 有效时间（秒），默认为 14 天（1209600 秒） 86400 1天
                        .userDetailsService(userDetailsService) // 用于加载用户信息
                        .rememberMeParameter("remember-me")   // 前端 checkbox 的 name 属性值
                        .alwaysRemember(false)                // 是否始终记住（忽略前端 checkbox）
                )
                .sessionManagement(session -> session
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                )
                .authenticationProvider(authenticationProvider()); // 启用自定义 provider


        return http.build();
    }

























      /*.formLogin(form -> form
                        .loginPage("/login")  // 自定义登录页路径
                        .loginProcessingUrl("/login")  // 登录请求处理 URL（POST）
                        .defaultSuccessUrl("/index")  // 登录成功跳转
                        .failureUrl("/login?error")    // 登录失败跳转
                        .permitAll()
                )*/






}