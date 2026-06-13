package de.skit.grocy.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private final JavaMailSender mailSender;
    private final MailProperties mailProperties;

    public MailService(ObjectProvider<JavaMailSender> mailSender, MailProperties mailProperties) {
        this.mailSender = mailSender.getIfAvailable();
        this.mailProperties = mailProperties;
    }

    public void sendVerificationMail(String to, String link) {
        send(to, "Grocy E-Mail bestätigen",
                "Willkommen bei Grocy!\n\nBitte bestätige deine E-Mail-Adresse über diesen Link:\n" + link);
    }

    public void sendPasswordResetMail(String to, String link) {
        send(to, "Grocy Passwort zurücksetzen",
                "Du kannst dein Passwort über diesen Link zurücksetzen:\n" + link
                        + "\n\nFalls du das nicht angefordert hast, kannst du diese Mail ignorieren.");
    }

    private void send(String to, String subject, String text) {
        if (!mailProperties.isEnabled()) {
            log.info("Mail sending disabled. Would send '{}' to {} with body:\n{}", subject, to, text);
            return;
        }

        if (mailSender == null) {
            throw new IllegalStateException("Mail sending is enabled, but no JavaMailSender is configured");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailProperties.getFrom());
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        try {
            mailSender.send(message);
        } catch (MailException ex) {
            throw new IllegalStateException("Could not send mail '" + subject + "' to " + to, ex);
        }
    }
}
