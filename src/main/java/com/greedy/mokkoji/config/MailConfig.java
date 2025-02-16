package com.greedy.mokkoji.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {
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
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        Properties properties = new Properties();
        properties.put("mail.transport,auth", auth);
        properties.put("mail.smtp.starttls.enable", starttls);
        properties.put("spring.mail.properties.mail.smtp.timeout", timeout);

        mailSender.setHost(host);
        mailSender.setUsername(userName);
        mailSender.setPassword(password);
        mailSender.setPort(port);
        mailSender.setDefaultEncoding(encoding);
        mailSender.setJavaMailProperties(properties);

        return mailSender;
    }
}
