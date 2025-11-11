package com.vortex.loginregister_new.service.impl;

import com.vortex.loginregister_new.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * 邮件服务实现
 * 只有在 JavaMailSender Bean 存在时才会创建此服务
 *
 * @author Vortex
 * @since 2024
 */
@Slf4j
@Service
@ConditionalOnBean(JavaMailSender.class)
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final String from;

    @Autowired
    public EmailServiceImpl(
            JavaMailSender mailSender,
            @Value("${spring.mail.username:}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    @Override
    public void sendVerificationCode(String to, String code) {
        String subject = "【Sun】验证码";
        
        String htmlContent = String.format(
            "<div style='padding: 20px; background-color: #f5f5f5;'>" +
            "<div style='max-width: 600px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1);'>" +
            "<h2 style='color: #2196f3; margin-bottom: 20px; text-align: center;'>Sun - 验证码</h2>" +
            "<p style='color: #333; font-size: 16px; line-height: 1.6;'>您好，</p>" +
            "<p style='color: #333; font-size: 16px; line-height: 1.6;'>您的验证码是：</p>" +
            "<div style='background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); padding: 20px; border-radius: 8px; text-align: center; margin: 20px 0;'>" +
            "<span style='font-size: 36px; font-weight: bold; color: white; letter-spacing: 8px;'>%s</span>" +
            "</div>" +
            "<p style='color: #666; font-size: 14px; line-height: 1.6;'>验证码 <strong>5分钟</strong> 内有效，请勿告知他人。</p>" +
            "<p style='color: #999; font-size: 12px; margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee;'>此邮件由系统自动发送，请勿回复。</p>" +
            "</div>" +
            "</div>",
            code
        );
        
        log.info("准备发送验证码邮件，收件人: {}, 发件人: {}", to, from);
        
        MimeMessage message = mailSender.createMimeMessage();
        
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("验证码邮件发送成功，收件人: {}", to);
        } catch (MessagingException e) {
            log.error("验证码邮件发送失败，收件人: {}, 错误: {}", to, e.getMessage());
            throw new RuntimeException("邮件发送失败: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("验证码邮件发送失败，收件人: {}, 错误: {}", to, e.getMessage());
            throw new RuntimeException("邮件发送失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void sendResetPasswordCode(String to, String code) {
        String subject = "【Sun】重置密码验证码";
        
        String htmlContent = String.format(
            "<div style='padding: 20px; background-color: #f5f5f5;'>" +
            "<div style='max-width: 600px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1);'>" +
            "<h2 style='color: #ff5722; margin-bottom: 20px; text-align: center;'>🔐 Sun - 密码重置</h2>" +
            "<p style='color: #333; font-size: 16px; line-height: 1.6;'>您好，</p>" +
            "<p style='color: #333; font-size: 16px; line-height: 1.6;'>您正在尝试重置密码，验证码是：</p>" +
            "<div style='background: linear-gradient(135deg, #ff6b6b 0%%, #feca57 100%%); padding: 20px; border-radius: 8px; text-align: center; margin: 20px 0;'>" +
            "<span style='font-size: 36px; font-weight: bold; color: white; letter-spacing: 8px;'>%s</span>" +
            "</div>" +
            "<p style='color: #666; font-size: 14px; line-height: 1.6;'>验证码 <strong>15分钟</strong> 内有效，请勿告知他人。</p>" +
            "<p style='color: #ff5722; font-size: 14px; line-height: 1.6;'><strong>⚠️ 如果您没有申请重置密码，请忽略此邮件。</strong></p>" +
            "<p style='color: #999; font-size: 12px; margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee;'>此邮件由系统自动发送，请勿回复。</p>" +
            "</div>" +
            "</div>",
            code
        );
        
        log.info("准备发送重置密码验证码邮件，收件人: {}, 发件人: {}", to, from);
        
        MimeMessage message = mailSender.createMimeMessage();
        
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("重置密码验证码邮件发送成功，收件人: {}", to);
        } catch (MessagingException e) {
            log.error("重置密码验证码邮件发送失败，收件人: {}, 错误: {}", to, e.getMessage());
            throw new RuntimeException("邮件发送失败: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("重置密码验证码邮件发送失败，收件人: {}, 错误: {}", to, e.getMessage());
            throw new RuntimeException("邮件发送失败: " + e.getMessage(), e);
        }
    }
}

