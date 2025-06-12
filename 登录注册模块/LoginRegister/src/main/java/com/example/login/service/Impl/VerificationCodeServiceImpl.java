package com.example.login.service.Impl;





import com.example.login.service.VerificationCodeService;
import com.example.login.util.DataValidationUtil;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



import java.util.concurrent.TimeUnit;


/**
 * 标注在服务实现类上，用于告诉 Spring 容器这个类是业务逻辑组件，需要被 Spring 托管。
 * 接口本身不能被实例化，Spring 无法管理它，这样做没有实际意义
 */
@Service
public class VerificationCodeServiceImpl implements VerificationCodeService {

    private final JavaMailSender javaMailSender;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String senderEmail;

    // 验证码缓存的键前缀
    private static final String VERIFICATION_CODE_PREFIX = "VerificationCode:";
    // 日志记录器
    private static final Logger logger = LoggerFactory.getLogger(VerificationCodeServiceImpl.class);

    /**
     * 构造注入依赖
     * @param javaMailSender
     * @param redisTemplate
     * @param templateEngine
     */
    @Autowired
    public VerificationCodeServiceImpl(JavaMailSender javaMailSender,
                                       RedisTemplate<String, Object> redisTemplate,
                                        SpringTemplateEngine templateEngine) {
        this.javaMailSender = javaMailSender;
        this.redisTemplate = redisTemplate;
        this.templateEngine = templateEngine;
    }

    /**
     * 发送验证码(根据邮箱)
     * @param email
     * @param random_code
     */
    public void sendVerificationCodeWithEmail(String email, String random_code) {
        try {
            // 创建 MimeMessage 对象用于发送 HTML 邮件
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            // 设置发件人、收件人、主题等
            helper.setFrom(senderEmail);
            helper.setTo(email);
            helper.setSubject("验证您的电子邮件");

            // 创建 Thymeleaf 上下文并设置变量
            Context context = new Context();
            context.setVariable("code", random_code);

            // 使用 Thymeleaf 模板引擎渲染 HTML
            String htmlContent = templateEngine.process("email", context); // email 是模板名称，对应 email.html

            // 发送 HTML 邮件
            helper.setText(htmlContent, true); // 第二个参数为 true 表示内容是 HTML 格式
            javaMailSender.send(mimeMessage);
        } catch (Exception e) {
            logger.error("邮件发送失败", e);
            throw new RuntimeException("邮件验证码发送失败，请稍后再试。", e);
        }
    }


    /**
     * 发送验证码(根据手机号)
     * @param phoneNumber
     * @param random_code
     */
    @Override
    public void sendVerificationCodeWithPhone(String phoneNumber, String random_code) {

    }

    /**
     * 存储验证码到Redis
     * @param email_phone
     * @param random_code
     * 设置有效期为3分钟, 设置键值对  键:"VerificationCodeWithEmail:" + email_phone  值:random_code
     */
    @Override
    public void storeVerificationCodeToRedis(String email_phone, String random_code){
        redisTemplate.opsForValue().set(
                VERIFICATION_CODE_PREFIX + email_phone,
                random_code,
                300, TimeUnit.SECONDS
        );
        logger.info("用户:{}    验证码:{} 已存储到Redis", email_phone,random_code);
    }

    /**
     * 验证验证码
     * @param email_phone
     * @param input_code
     * @return true/false
     */
    @Override
    public boolean validateVerificationCode(String email_phone, String input_code) {
    //根据VERIFICATION_CODE_PREFIX + email_phone组成的唯一键取出对应的值
    Object storedCodeObject = redisTemplate.opsForValue().get(VERIFICATION_CODE_PREFIX + email_phone);
    String storedCodeString = storedCodeObject.toString();


    if (!storedCodeString.equals(input_code)) {
        logger.warn("验证码错误");
        return false;
    }

    redisTemplate.delete(VERIFICATION_CODE_PREFIX + email_phone);
    logger.info("账号:{}验证成功", email_phone);
    return true;
}




































}
