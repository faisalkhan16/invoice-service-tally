package com.invoice.service;

import com.invoice.dto.EmailDetailsDTO;
import com.invoice.model.Email;
import com.invoice.repository.EmailRepository;
import com.invoice.util.CommonUtils;
import com.invoice.util.Constants;
import com.invoice.util.PDFFileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Properties;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    @Value("${MAIL_HOST}")
    private String MAIL_HOST;

    @Value("${MAIL_PORT}")
    private String MAIL_PORT;

    @Value("${MAIL_USERNAME}")
    private String MAIL_USERNAME;

    @Value("${MAIL_PASSWORD}")
    private String MAIL_PASSWORD;

    @Value("${MAIL_SENDER}")
    private String MAIL_SENDER;

    @Autowired
    @Qualifier("SQLService")
    private EmailRepository emailRepository;

    private final InvoiceService invoiceService;

    // private final JavaMailSender javaMailSender;
    private final PDFFileUtil pdfUtil;
    public void sendEmails(){
        List<Email> emailList = emailRepository.getPendingEmails();
        for (Email email:emailList){
            emailRepository.updateEmailsStatus(Constants.TRANSPOSE_STATUS,email.getSeqId());
            String pdf = invoiceService.getPDF(email.getId());
            if(!CommonUtils.isNullOrEmptyString(pdf)){
                emailRepository.updateEmailsStatus(Constants.PROCESSED_STATUS,email.getSeqId());
                File file = pdfUtil.generateFile(pdf,email.getId());

                StringBuffer emailSubject =  new StringBuffer();
                emailSubject.append("E-Invoice ").append(email.getId());

                StringBuffer emailBody =  new StringBuffer();
                emailBody.append("E-Invoice ").append(email.getId());

                var emailDetailsDTO = EmailDetailsDTO
                        .builder()
                        .subject(emailSubject.toString())
                        .msgBody(emailBody.toString())
                        .attachment(file)
                        .recipient(email.getBuyerEmail()).build();

                if(sendMailWithAttachmentAuth(emailDetailsDTO)){
                    emailRepository.updateEmailsStatus(Constants.PROCESSED_STATUS,email.getSeqId());
                }else {
                    emailRepository.updateEmailsStatus(Constants.FAILURE_STATUS,email.getSeqId());
                }
            }else{
                log.error("EmailService sendEmails() pdf Does not find invoiceID: {}",email.getId());
                emailRepository.updateEmailsStatus(Constants.CREATED_STATUS,email.getSeqId());
            }
        }
    }

    private boolean sendMailWithAttachmentAuth(EmailDetailsDTO details){
        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", MAIL_HOST); //SMTP Host
            props.put("mail.smtp.socketFactory.port", MAIL_PORT); //SSL Port
            props.put("mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory"); //SSL Factory Class
            props.put("mail.smtp.auth", "true"); //Enabling SMTP Authentication
            props.put("mail.smtp.port", MAIL_PORT); //SMTP Port

            Authenticator auth = new Authenticator() {
                //override the getPasswordAuthentication method
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(MAIL_USERNAME, MAIL_PASSWORD);
                }
            };

            Session session = Session.getDefaultInstance(props, auth);

            MimeMessage msg = new MimeMessage(session);
            msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
            msg.addHeader("format", "flowed");
            msg.addHeader("Content-Transfer-Encoding", "8bit");

            msg.setFrom(new InternetAddress(MAIL_SENDER, "NoReply"));

            msg.setSubject(details.getSubject(), "UTF-8");

            msg.setSentDate(new Date());

            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(details.getRecipient(), false));

            BodyPart messageBodyPart = new MimeBodyPart();

            messageBodyPart.setText(details.getMsgBody());

            Multipart multipart = new MimeMultipart();

            multipart.addBodyPart(messageBodyPart);

            messageBodyPart = new MimeBodyPart();
            String filename = details.getAttachment().getPath();
            DataSource source = new FileDataSource(filename);
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(filename);
            multipart.addBodyPart(messageBodyPart);

            msg.setContent(multipart);

            Transport.send(msg);
            log.info("EmailService send email details: {}",details);
            return true;
        }catch (Exception ex){
            ex.printStackTrace();
            return false;
        }
    }

    @Scheduled(cron ="${CRON_EMAIL_SENDER}")
    private void jobSendEmail(){
        log.info("jobSendEmail at {}", LocalDateTime.now());
        sendEmails();
    }

}
