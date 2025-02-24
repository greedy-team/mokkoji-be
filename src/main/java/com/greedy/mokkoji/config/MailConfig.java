package com.greedy.mokkoji.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {
    private static final String AUTH_KEY = "mail.transport.auth";
    private static final String TLS_KEY = "mail.smtp.starttls.enable";
    private static final String TIMEOUT_KEY = "spring.mail.properties.mail.smtp.timeout";
    @Value("${spring.mail.host}")
    private String host;
    @Value("${spring.mail.username}")
    private String userName;
    @Value("${spring.mail.password}")
    private String password;
    @Value("${spring.mail.properties.mail.smtp.auth}")
    private boolean auth;
    @Value("${spring.mail.port}")
    private int port;
    @Value("${spring.mail.properties.mail.smtp.starttls.enable}")
    private boolean starttls;
    @Value("${spring.mail.properties.mail.smtp.timeout}")
    private int timeout;
    @Value("${spring.mail.default-encoding}")
    private String encoding;

    @Bean
    public JavaMailSender getMailSender() {
        final JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        Properties properties = new Properties();
        properties.put(AUTH_KEY, auth);
        properties.put(TLS_KEY, starttls);
        properties.put(TIMEOUT_KEY, timeout);

        mailSender.setHost(host);
        mailSender.setUsername(userName);
        mailSender.setPassword(password);
        mailSender.setPort(port);
        mailSender.setDefaultEncoding(encoding);
        mailSender.setJavaMailProperties(properties);

        return mailSender;
    }
}
