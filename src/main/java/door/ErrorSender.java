package door;

import fox.out.Out;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;


public class ErrorSender {
    private static String from = "angelicalis39@mail.ru";
    private static String host = "smtp.mail.ru";
    private static String port = "465"; // POP3 — 995, SMTP — 465

    private ErrorSender() {}

    public static void send(String message) {
        final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";

        Properties properties = System.getProperties();
        properties.setProperty("mail.smtp.host", host);
        properties.setProperty("mail.smtp.port", port);
        properties.setProperty("mail.smtp.socketFactory.class", SSL_FACTORY);
        properties.setProperty("mail.smtp.socketFactory.fallback", "false");
        properties.setProperty("mail.smtp.socketFactory.port", port);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.ssl.protocols", "TLSv1.2");
        properties.put("mail.smtp.ssl.trust", "*");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.starttls.required", "true");
        properties.put("mail.debug", "true");
        properties.put("mail.store.protocol", "pop3");
        properties.put("mail.transport.protocol", "smtp");

        Session session = Session.getDefaultInstance(properties,
                new Authenticator(){
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication("angelicalis39@mail.ru", "uTDN6bfRjV5Qsj4jEdS4");
                    }});

        try {
            MimeMessage mime = new MimeMessage(session); // email message
            mime.setFrom(new InternetAddress(from)); // setting header fields
            mime.addRecipient(Message.RecipientType.TO, new InternetAddress("angelicalis39@mail.ru"));
            mime.setSubject("BackVocalStudio has error"); // subject line
            mime.setText(message);

            // Send message
            Transport.send(mime);
        } catch (MessagingException mex){
            Out.Print(ErrorSender.class, Out.LEVEL.ERROR, "Exception by mail send: " + mex.getMessage());
            mex.printStackTrace();
        }
    }
}
