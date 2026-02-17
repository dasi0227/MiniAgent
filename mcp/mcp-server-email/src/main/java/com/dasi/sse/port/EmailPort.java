package com.dasi.sse.port;

import com.dasi.mcp.dto.SendEmailToolRequest;
import com.dasi.mcp.dto.SendEmailToolResponse;
import com.dasi.mcp.port.IEmailPort;
import com.dasi.sse.properties.EmailProperties;
import com.dasi.type.util.SecretHeaderUtil;
import jakarta.annotation.Resource;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Properties;


@Slf4j
@Service
public class EmailPort implements IEmailPort {

    @Resource
    private JavaMailSender javaMailSender;

    @Resource
    private EmailProperties emailProperties;

    @Override
    public SendEmailToolResponse sendEmail(SendEmailToolRequest toolRequest) throws UnsupportedEncodingException {

        SendEmailToolResponse toolResponse = new SendEmailToolResponse();
        Map<String, String> secretMap = SecretHeaderUtil.getSecretMap();

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

        String fromAddress = SecretHeaderUtil.resolve(secretMap, "fromAddress", emailProperties.getFromAddress());
        String fromName = SecretHeaderUtil.resolve(secretMap, "fromName", emailProperties.getFromName());
        JavaMailSender activeMailSender = resolveMailSender(secretMap);

        if (!StringUtils.hasText(fromAddress) || activeMailSender == null) {
            toolResponse.setCode(500);
            toolResponse.setInfo("Email 参数未配置");
            return toolResponse;
        }

        InternetAddress internetAddress = new InternetAddress(fromAddress, fromName, "UTF-8");

        try {
            MimeMessage mimeMessage = activeMailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            messageHelper.setFrom(internetAddress);
            messageHelper.setTo(toolRequest.getTo());
            messageHelper.setSubject(toolRequest.getSubject());

            boolean html = Boolean.TRUE.equals(toolRequest.getHtml());
            messageHelper.setText(toolRequest.getContent(), html);

            activeMailSender.send(mimeMessage);

            toolResponse.setCode(200);
            toolResponse.setInfo("Email 发送成功");
            toolResponse.setMessageId(mimeMessage.getMessageID());
            log.info("调用 SMTP 发送邮件成功：to={} subject={} from={}", toolRequest.getTo(), toolRequest.getSubject(), internetAddress);
            return toolResponse;

        } catch (MailException | jakarta.mail.MessagingException e) {
            toolResponse.setCode(500);
            toolResponse.setInfo("Email 发送失败: " + e.getMessage());
            log.error("调用 SMTP 发送邮件失败：to={} subject={} error={}", toolRequest.getTo(), toolRequest.getSubject(), e.getMessage(), e);
            return toolResponse;
        }
    }

    private JavaMailSender resolveMailSender(Map<String, String> secretMap) {
        String smtpHost = SecretHeaderUtil.resolve(secretMap, "smtpHost", null);
        String smtpPort = SecretHeaderUtil.resolve(secretMap, "smtpPort", null);
        String smtpUsername = SecretHeaderUtil.resolve(secretMap, "smtpUsername", null);
        String smtpPassword = SecretHeaderUtil.resolve(secretMap, "smtpPassword", null);

        if (!StringUtils.hasText(smtpHost) || !StringUtils.hasText(smtpPort) || !StringUtils.hasText(smtpUsername) || !StringUtils.hasText(smtpPassword)) {
            return javaMailSender;
        }

        Integer port = parseInteger(smtpPort);
        if (port == null) {
            return null;
        }

        JavaMailSenderImpl dynamicMailSender = new JavaMailSenderImpl();
        dynamicMailSender.setHost(smtpHost);
        dynamicMailSender.setPort(port);
        dynamicMailSender.setUsername(smtpUsername);
        dynamicMailSender.setPassword(smtpPassword);
        dynamicMailSender.setProtocol("smtp");
        dynamicMailSender.setDefaultEncoding("UTF-8");

        Properties properties = dynamicMailSender.getJavaMailProperties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.starttls.required", "true");
        properties.put("mail.smtp.connectiontimeout", "15000");
        properties.put("mail.smtp.timeout", "15000");
        properties.put("mail.smtp.writetimeout", "15000");

        return dynamicMailSender;
    }

    private Integer parseInteger(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception ignored) {
            return null;
        }
    }

}
