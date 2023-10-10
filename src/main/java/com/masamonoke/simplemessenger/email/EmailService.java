package com.masamonoke.simplemessenger.email;

import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class EmailService implements EmailSender {
    private final JavaMailSender javaMailSender;
    @Override
    @Async
    public void send(String to, String email) {
        try {
            var mimeMessage = javaMailSender.createMimeMessage();
            var mimeMessageHelper = new MimeMessageHelper(mimeMessage, "utf-8");
            mimeMessageHelper.setText(email, true);
            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setSubject("Confirm your email");
            mimeMessageHelper.setFrom("simplemessenger@email.com");
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            log.error("Failed to send email", e);
        }
    }
}
