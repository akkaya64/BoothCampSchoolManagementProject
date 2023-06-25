package com.schoolmanagement.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

@Service
public class EmailService {

    private static String MAIL_ADDRESS;
    private static String PASSWORD;

    public static void sendMail(String recipient , String mailMessage, String subject) throws MessagingException {

        Properties properties = new Properties();

        // !!! Gmail SMTP sunucuzunu kullanarak e-posta gondemrek icin yapilandiriliyor
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.ssl.protocols", "TLSv1.2");
        properties.put("mail.smtp.ssl.enable", "false");


        // Authentication Islemi
        // Gmail gibi guveligi on planda tutan yapilar email gibi password gibi yapilari aciktan gondermek yerine
        // Authentication yaparak gonderir. bu bilgilerin application.properties den alan yapi
        // PasswordAuthentication() methodu
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(MAIL_ADDRESS,PASSWORD);
            }
        });

        // javax.mail kutuphanesinden Message interface sini cagiriyoruz message objesine
        // yukarida olusturdugumuz Authenticate edilmis MAIL_ADDRESS,PASSWORD session objesini ve MAIL_ADDRESS,
        // recipient,mailMessage,subject datalarini setliyoruz bu setleme islemini yapan methodu bu scope nin disinda
        // prepareMessage adinda bir method yazarak burada prepareMessage methodunu cagirip yukarida yazdigimiz
        // datalari setliyoruz. yani
        Message message =  prepareMessage(session, MAIL_ADDRESS, recipient,mailMessage,subject);
        Transport.send(message);//Mesajin gonderlme islemi yapildi

    }

    // Email uzerinden gonrecegimiz Message data type indaki prepareMessage objesini olusturuyoruz.
    private static Message prepareMessage(Session session, String from, String recipient,
                                          String mailMessage, String subject) throws MessagingException {
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));// kim tarafindan gonderilecek
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipient));//Kime gidecekse
        message.setText(mailMessage);
        message.setSubject(subject);
        return message;

    }

    @Value("${email.address}")//properties{email.address} deki email i bu class icindeki MAIL_ADDRESS degiskenine veriyor
    public void setEmail(String email){//
        MAIL_ADDRESS = email;
    }

    @Value("${email.password}")//properties{email.password} deki passwordu i Bu class icindeki PASSWORD
    // degiskenine veriyor
    public void setPassword(String password){
        PASSWORD = password;
    }

}