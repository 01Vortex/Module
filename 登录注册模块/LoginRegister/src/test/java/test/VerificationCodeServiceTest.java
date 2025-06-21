package test;


import com.example.login.LoginApplication;
import com.example.login.service.VerificationCodeService;
import com.example.login.utils.RandomCodeUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;


import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = LoginApplication.class)
public class VerificationCodeServiceTest {


    @Autowired
    private VerificationCodeService verificationCodeService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private String testEmail = "2055314849@qq.com";
    private String testCode = RandomCodeUtil.generateNumericCode(6);

    private static final Logger logger = LoggerFactory.getLogger(VerificationCodeServiceTest.class);

    @BeforeEach
    public void setUp() {
        // 清理 Redis 中的旧数据
        redisTemplate.delete("VerificationCode:" + testEmail);
    }


    // 测试发送验证码
    @Test
    public void testSendVerificationCodeWithEmail() {
        // 由于发送邮件无法断言结果，仅验证是否不抛出异常
        verificationCodeService.sendVerificationCodeWithEmail(testEmail, testCode);
        logger.info("已发送验证码:{}到邮箱：{}",testCode, testEmail);

        // 可选：检查 Redis 是否存储了验证码
        verificationCodeService.storeVerificationCodeToRedis(testEmail, testCode);

        Object storedCode = redisTemplate.opsForValue().get("VerificationCode:" + testEmail);
        assertTrue(storedCode.toString().equals(testCode));
    }

    // 测试验证码验证
    @Test
    public void testValidateVerificationCode_Success() {
        // 存储验证码到 Redis
        verificationCodeService.storeVerificationCodeToRedis(testEmail, testCode);

        // 验证正确的验证码
        boolean result = verificationCodeService.validateVerificationCode(testEmail, testCode);
        assertTrue(result);
    }


}
