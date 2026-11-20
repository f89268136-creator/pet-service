package com.pet.service;

import com.pet.constant.MessageConstant;
import com.pet.exception.LoginFailedException;
import com.pet.vo.SendCodeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.regex.Pattern;

/**
 * 邮箱验证码服务。
 *
 * <p>验证码写入 Redis 并设置有效期；开启邮件发送时通过 SMTP 投递，
 * 未开启时仅记录日志并可由 {@code dev-return-code} 直接返回给前端联调。</p>
 */
@Service
@Slf4j
public class LoginCodeService {

    private static final String CODE_KEY_PREFIX = "pet:login:code:";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private LoginRateLimiter loginRateLimiter;
    @Autowired
    private JavaMailSender mailSender;

    @Value("${pet.login.code-ttl-seconds:300}")
    private long codeTtlSeconds;
    @Value("${pet.login.mail-enabled:false}")
    private boolean mailEnabled;
    @Value("${pet.login.dev-return-code:true}")
    private boolean devReturnCode;
    @Value("${pet.login.from:}")
    private String from;
    @Value("${spring.mail.username:}")
    private String mailUsername;

    /**
     * 生成并发送验证码，返回有效期及开发环境验证码。
     */
    public SendCodeVO send(String email) {
        String target = normalizeEmail(email);
        loginRateLimiter.assertSendAllowed(target);

        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        stringRedisTemplate.opsForValue()
                .set(codeKey(target), code, Duration.ofSeconds(codeTtlSeconds));

        if (mailEnabled) {
            try {
                sendMail(target, code);
            } catch (Exception e) {
                stringRedisTemplate.delete(codeKey(target));
                log.error("验证码邮件发送失败，email={}", target, e);
                throw new LoginFailedException(MessageConstant.CODE_SEND_FAILED);
            }
        } else {
            log.info("验证码邮件未开启，email={}，code={}", target, code);
        }

        loginRateLimiter.recordSend(target);
        return SendCodeVO.builder()
                .expireSeconds((int) codeTtlSeconds)
                .devCode(mailEnabled || !devReturnCode ? null : code)
                .build();
    }

    /**
     * 校验验证码；校验成功后立即删除，失败则计入风控。
     *
     * @return 规范化后的邮箱
     */
    public String verify(String email, String code) {
        String target = normalizeEmail(email);
        loginRateLimiter.assertNotBanned(target);
        if (!StringUtils.hasText(code)) {
            throw new LoginFailedException(MessageConstant.CODE_REQUIRED);
        }
        String cached = stringRedisTemplate.opsForValue().get(codeKey(target));
        if (cached == null) {
            throw new LoginFailedException(MessageConstant.CODE_EXPIRED);
        }
        if (!cached.equals(code.trim())) {
            loginRateLimiter.recordFailure(target);
            throw new LoginFailedException(MessageConstant.CODE_ERROR);
        }
        stringRedisTemplate.delete(codeKey(target));
        loginRateLimiter.clearFailures(target);
        return target;
    }

    private void sendMail(String email, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        String sender = StringUtils.hasText(from) ? from : mailUsername;
        if (StringUtils.hasText(sender)) {
            message.setFrom(sender);
        }
        message.setTo(email);
        message.setSubject("【宠爱有家】登录验证码");
        message.setText("您的登录验证码为：" + code + "，"
                + Math.max(1, codeTtlSeconds / 60) + " 分钟内有效。如非本人操作请忽略。");
        mailSender.send(message);
    }

    private String normalizeEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new LoginFailedException(MessageConstant.EMAIL_REQUIRED);
        }
        String target = email.trim().toLowerCase();
        if (!EMAIL_PATTERN.matcher(target).matches()) {
            throw new LoginFailedException(MessageConstant.EMAIL_FORMAT_ERROR);
        }
        return target;
    }

    private String codeKey(String email) {
        return CODE_KEY_PREFIX + email;
    }
}