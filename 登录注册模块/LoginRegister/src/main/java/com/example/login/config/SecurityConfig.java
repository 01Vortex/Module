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
     * 定义 Spring Security 的安全策略配置(认证 授权 过滤器链 会话管理  异常处理)
     * @ param HttpSecurity http (构建安全策略的 DSL 工具类，用于配置各种安全规则)
     * @return securityFilterChain (Spring Security 的安全过滤器链对象)
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // 禁用 CSRF 保护

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/login","/api/auth/register", "/api/auth/reset-password").permitAll() // 允许登录和注册
                        .requestMatchers("/api/verify/send-code-email","/api/verify/send-code-phone").permitAll()          //  允许发送验证码
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()                                  // 允许访问 swagger-ui 和 api-docs
                        .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")                                             // 允许访问 /admin 路径下的资源，需要具有 ROLE_ADMIN 权限
                        .anyRequest().authenticated()                                                                          // 其他任何请求都需要经过身份验证
                )

                //
                .logout(logout -> logout
                        .logoutUrl("/logout")                                                   // 自定义登出 URL
                        .logoutSuccessHandler((request, response, authentication) -> { //
                            response.setStatus(HttpServletResponse.SC_OK);                      // 设置响应状态码为 200
                            response.getWriter().write("{\"message\": \"登出成功\"}");         // 响应内容
                        })
                        .deleteCookies("JSESSIONID")                          // 删除客户端的 JSESSIONID
                        .invalidateHttpSession(true)                                            // 使会话无效
                )



                .rememberMe(remember -> remember
                        .rememberMeCookieName("remember-me")                         //  设置记住我 cookie 名称
                        .tokenRepository(rememberMeTokenRepository())                // 使用数据库存储
                        .key(RandomCodeUtil.generateAlphaNumericKey(20))      // 可选：自定义 key，默认使用安全随机生成的 key
                        .tokenValiditySeconds(15)                                    // 设置 token 有效时间（秒），默认为 14 天（1209600 秒） 86400 1天
                        .userDetailsService(userDetailsService)                      // 用于加载用户信息
                        .rememberMeParameter("remember-me")                          // 前端 checkbox 的 name 属性值
                        .alwaysRemember(false)                                       // 是否始终记住（忽略前端 checkbox）
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)      //  禁用 session ,会话策略为无状态（STATELESS），不创建或使用HTTP会话
                )
                .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthEntryPoint))  // 指定认证失败时的入口类 JwtAuthEntryPoint。
                .addFilterBefore(new JwtFilter(), UsernamePasswordAuthenticationFilter.class)           // 将你自定义的 JwtFilter 插入到 UsernamePasswordAuthenticationFilter 之前执行
                .authenticationProvider(authenticationProvider());                                      // 启用自定义 provider


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