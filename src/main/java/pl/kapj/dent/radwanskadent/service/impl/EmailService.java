package pl.kapj.dent.radwanskadent.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import pl.kapj.dent.radwanskadent.service.IEmailService;

@Service
public class EmailService implements IEmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender javaMailSender;
    private final String senderEmail;

    public EmailService(JavaMailSender javaMailSender,
                        @Value("${mail.username}") String senderEmail) {
        this.javaMailSender = javaMailSender;
        this.senderEmail = senderEmail;
    }

    @Async
    @Override
    public void sendMail(String receiver, String subject, String content) {
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom(senderEmail);
            mail.setTo(receiver);
            mail.setSubject(subject);
            mail.setText(content);
            javaMailSender.send(mail);
        } catch (Exception e) {
            log.error("Błąd wysyłki maila do {}: {}", receiver, e.getMessage(), e);
        }
    }
}
