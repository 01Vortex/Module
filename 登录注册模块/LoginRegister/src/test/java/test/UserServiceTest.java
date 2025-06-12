package test;


import com.example.login.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.example.login.service.UserService;


import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserService userService; // 模拟接口实现

    User user = new User();
    String emailOrPhone = "2055314849@qq.com";
    String newPassword = "6666666";
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user.setEmail("2055314849@qq.com");
        user.setPhoneNumber("15326545665");
        user.setPassword("111111");
    }



    @Test
    void testCreateAccount() {
        userService.createAccount(user);
        verify(userService, times(1)).createAccount(user);
    }

    @Test
    void testResetPassword() {


        // 调用接口方法
        userService.resetPassword(emailOrPhone, newPassword);

        // 验证 resetPassword 是否被调用一次
        verify(userService, times(1)).resetPassword(emailOrPhone, newPassword);
    }
}
