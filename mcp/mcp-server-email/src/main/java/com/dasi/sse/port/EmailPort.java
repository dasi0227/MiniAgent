package com.dasi.sse.port;

import com.dasi.credential.EmailCredential;
import com.dasi.credential.McpHeaderContext;
import com.dasi.mcp.dto.SendEmailToolRequest;
import com.dasi.mcp.dto.SendEmailToolResponse;
import com.dasi.mcp.port.IEmailPort;
import jakarta.annotation.Resource;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

@Slf4j
@Service
public class EmailPort implements IEmailPort {

    @Resource
    private McpHeaderContext mcpHeaderContext;

    @Override
    public SendEmailToolResponse sendEmail(SendEmailToolRequest toolRequest) throws UnsupportedEncodingException {

        SendEmailToolResponse toolResponse = new SendEmailToolResponse();
        EmailCredential credential = resolveCredential(toolResponse);
        if (credential == null) {
            return toolResponse;
        }

        if (toolRequest == null) {
            toolResponse.setCode(500);
            toolResponse.setInfo("Email 请求为空");
            return toolResponse;
        }

        if (!StringUtils.hasText(toolRequest.getTo())) {
            toolResponse.setCode(500);
            toolResponse.setInfo("Email 收件人不能为空");
            return toolResponse;
        }

        if (!StringUtils.hasText(toolRequest.getSubject())) {
            toolResponse.setCode(500);
            toolResponse.setInfo("Email 主题不能为空");
            return toolResponse;
        }

        if (!StringUtils.hasText(toolRequest.getContent())) {
            toolResponse.setCode(500);
            toolResponse.setInfo("Email 正文不能为空");
            return toolResponse;
        }

        String fromAddress = credential.getFromAddress();
        String fromName = credential.getFromName();
        InternetAddress internetAddress = new InternetAddress(fromAddress, fromName, "UTF-8");

        try {
            JavaMailSenderImpl javaMailSender = buildMailSender(credential);
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            messageHelper.setFrom(internetAddress);
            messageHelper.setTo(toolRequest.getTo());
            messageHelper.setSubject(toolRequest.getSubject());

            boolean html = Boolean.TRUE.equals(toolRequest.getHtml());
            messageHelper.setText(toolRequest.getContent(), html);

            javaMailSender.send(mimeMessage);

            toolResponse.setCode(200);
            toolResponse.setInfo("Email 发送成功");
            toolResponse.setMessageId(mimeMessage.getMessageID());
            log.info("调用 SMTP 发送邮件成功：to={} subject={} userId={}", toolRequest.getTo(), toolRequest.getSubject(), credential.getUserId());
            return toolResponse;

        } catch (MailException | jakarta.mail.MessagingException e) {
            toolResponse.setCode(500);
            toolResponse.setInfo("Email 发送失败: " + e.getMessage());
            log.error("调用 SMTP 发送邮件失败：to={} subject={} error={}", toolRequest.getTo(), toolRequest.getSubject(), e.getMessage(), e);
            return toolResponse;
        }
    }

    private EmailCredential resolveCredential(SendEmailToolResponse toolResponse) {
        EmailCredential credential = mcpHeaderContext.getCredential();
        if (credential == null || !credential.checkValid()) {
            toolResponse.setCode(400);
            toolResponse.setInfo("头部信息缺失或非法，必须包含 smtpHost/smtpPort/smtpUsername/smtpPassword/fromAddress/fromName/userId");
            return null;
        }
        return credential;
    }

    private JavaMailSenderImpl buildMailSender(EmailCredential credential) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(credential.getSmtpHost());
        sender.setPort(credential.getSmtpPort());
        sender.setUsername(credential.getSmtpUsername());
        sender.setPassword(credential.getSmtpPassword());
        sender.setDefaultEncoding("UTF-8");

        Properties props = sender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.ssl.enable", "false");
        props.put("mail.smtp.connectiontimeout", "15000");
        props.put("mail.smtp.timeout", "15000");
        props.put("mail.smtp.writetimeout", "15000");
        return sender;
    }

}
