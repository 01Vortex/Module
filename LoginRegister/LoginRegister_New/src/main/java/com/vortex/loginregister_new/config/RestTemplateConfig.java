package com.vortex.loginregister_new.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;

/**
 * RestTemplate 配置类
 * 配置超时时间和可选的代理支持
 *
 * @author Vortex
 * @since 2024
 */
@Slf4j
@Configuration
public class RestTemplateConfig {

    @Value("${rest.template.connect-timeout:10000}")
    private int connectTimeout;

    @Value("${rest.template.read-timeout:30000}")
    private int readTimeout;

    @Value("${rest.template.proxy.enabled:false}")
    private boolean proxyEnabled;

    @Value("${rest.template.proxy.host:}")
    private String proxyHost;

    @Value("${rest.template.proxy.port:0}")
    private int proxyPort;

    @Value("${rest.template.proxy.type:HTTP}")
    private String proxyType;

    @Value("${rest.template.proxy.use-system-proxy:false}")
    private boolean useSystemProxy;

    @Bean
    public RestTemplate restTemplate() {
        ClientHttpRequestFactory factory = createRequestFactory();
        RestTemplate restTemplate = new RestTemplate(factory);
        log.info("RestTemplate 已配置 - 连接超时: {}ms, 读取超时: {}ms, 代理: {}", 
                connectTimeout, readTimeout, proxyEnabled ? proxyHost + ":" + proxyPort : "未启用");
        return restTemplate;
    }

    private ClientHttpRequestFactory createRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);

        // 优先使用系统代理（如果启用）
        if (useSystemProxy) {
            String httpProxy = System.getProperty("http.proxyHost");
            String httpProxyPort = System.getProperty("http.proxyPort");
            String httpsProxy = System.getProperty("https.proxyHost");
            String httpsProxyPort = System.getProperty("https.proxyPort");
            
            if (httpProxy != null && httpProxyPort != null) {
                try {
                    int port = Integer.parseInt(httpProxyPort);
                    Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(httpProxy, port));
                    factory.setProxy(proxy);
                    log.info("RestTemplate 使用系统HTTP代理 - 主机: {}, 端口: {}", httpProxy, port);
                    testProxyConnection(httpProxy, port);
                    return factory;
                } catch (Exception e) {
                    log.warn("使用系统HTTP代理失败: {}", e.getMessage());
                }
            }
            
            if (httpsProxy != null && httpsProxyPort != null) {
                try {
                    int port = Integer.parseInt(httpsProxyPort);
                    Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(httpsProxy, port));
                    factory.setProxy(proxy);
                    log.info("RestTemplate 使用系统HTTPS代理 - 主机: {}, 端口: {}", httpsProxy, port);
                    testProxyConnection(httpsProxy, port);
                    return factory;
                } catch (Exception e) {
                    log.warn("使用系统HTTPS代理失败: {}", e.getMessage());
                }
            }
            
            log.warn("系统代理未配置，将使用手动配置的代理或直连");
        }

        // 使用手动配置的代理
        if (proxyEnabled && proxyHost != null && !proxyHost.trim().isEmpty() && proxyPort > 0) {
            try {
                Proxy.Type type = Proxy.Type.HTTP;
                if ("SOCKS".equalsIgnoreCase(proxyType)) {
                    type = Proxy.Type.SOCKS;
                }
                Proxy proxy = new Proxy(type, new InetSocketAddress(proxyHost, proxyPort));
                factory.setProxy(proxy);
                log.info("RestTemplate 代理已配置 - 类型: {}, 主机: {}, 端口: {}", type, proxyHost, proxyPort);
                testProxyConnection(proxyHost, proxyPort);
            } catch (Exception e) {
                log.error("配置代理失败，将使用直连: {}", e.getMessage());
                log.error("请检查: 1) 代理服务器是否运行 2) 代理地址和端口是否正确 3) 防火墙是否阻止连接");
            }
        } else if (proxyEnabled) {
            log.warn("代理已启用但配置不完整 - host: {}, port: {}", proxyHost, proxyPort);
        }

        return factory;
    }

    /**
     * 测试代理连接
     */
    private void testProxyConnection(String host, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 3000);
            log.info("✅ 代理连接测试成功 - {}:{}", host, port);
        } catch (Exception e) {
            log.error("❌ 代理连接测试失败 - {}:{} - 错误: {}", host, port, e.getMessage());
            log.error("请确认: 1) 代理服务器正在运行 2) 端口号正确 3) 防火墙允许连接");
        }
    }
}

