package cz.gennario.gennarioframework.emailing;

import cz.gennario.gennarioframework.utils.replacement.ReplacementPackage;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class EmailAPI {

    private static String email;
    private static String password;

    private static String smtp;
    private static int port;

    public EmailAPI(String email, String password, String smtp, int port) {
        EmailAPI.email = email;
        EmailAPI.password = password;
        EmailAPI.smtp = smtp;
        EmailAPI.port = port;
    }

    public static void sendEmail(String to, String subject, String text, ReplacementPackage replacementPackage) {
        Email emailInstance = EmailBuilder.startingBlank()
                .from("PolyCrew.net", email)
                .to(to)
                .withSubject(subject)
                .withPlainText(replacementPackage.replace(text))
                .buildEmail();

        Mailer mailer = MailerBuilder
                .withSMTPServer(smtp, port, email, password)
                .withTransportStrategy(TransportStrategy.SMTP_TLS)
                .clearEmailValidator()
                .async()
                .buildMailer();

        mailer.sendMail(emailInstance);
    }

    public static void sendEmail(String to, String subject, File htmlFile, ReplacementPackage replacementPackage) {
        try {
            String htmlContent = new String(Files.readAllBytes(Paths.get(htmlFile.toURI())));

            htmlContent = replacementPackage.replace(htmlContent);

            Email emailInstance = EmailBuilder.startingBlank()
                    .from("PolyCrew.net", email)
                    .to(to)
                    .withSubject(subject)
                    .withHTMLText(htmlContent)
                    .buildEmail();

            Mailer mailer = MailerBuilder
                    .withSMTPServer(smtp, port, email, password)
                    .withTransportStrategy(TransportStrategy.SMTP_TLS)
                    .clearEmailValidator()
                    .async()
                    .buildMailer();

            mailer.sendMail(emailInstance);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
