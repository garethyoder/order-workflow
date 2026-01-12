package com.cedarmeadowmeats.orderworkflow.sendemailconfirmation.service;


import com.cedarmeadowmeats.orderworkflow.sendemailconfirmation.config.DjTemplateLocationConfig;
import com.cedarmeadowmeats.orderworkflow.sendemailconfirmation.config.EmailTemplateLocationConfig;
import com.cedarmeadowmeats.orderworkflow.sendemailconfirmation.model.EmailTemplate;
import com.cedarmeadowmeats.orderworkflow.sendemailconfirmation.model.OrganizationIdEnum;
import com.cedarmeadowmeats.orderworkflow.sendemailconfirmation.model.Submission;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.annotations.NotNull;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.Body;
import software.amazon.awssdk.services.sesv2.model.Content;
import software.amazon.awssdk.services.sesv2.model.Destination;
import software.amazon.awssdk.services.sesv2.model.EmailContent;
import software.amazon.awssdk.services.sesv2.model.Message;
import software.amazon.awssdk.services.sesv2.model.SendEmailRequest;
import software.amazon.awssdk.services.sesv2.model.SendEmailResponse;
import software.amazon.awssdk.services.sesv2.model.SesV2Exception;

@Service
@EnableConfigurationProperties({EmailTemplateLocationConfig.class, DjTemplateLocationConfig.class})
public class EmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final SesV2Client sesV2Client;

    private final EmailTemplateLocationConfig emailTemplateLocationConfig;

    private final DjTemplateLocationConfig djTemplateLocationConfig;

    public EmailService(SesV2Client sesV2Client,
                        EmailTemplateLocationConfig emailTemplateLocationConfig,
                        DjTemplateLocationConfig djTemplateLocationConfig) {
        this.sesV2Client = sesV2Client;
        this.emailTemplateLocationConfig = emailTemplateLocationConfig;
        this.djTemplateLocationConfig = djTemplateLocationConfig;
    }

    public SendEmailResponse sendEmailConfirmationToClient(final Submission submission) throws SesV2Exception {

        EmailTemplate template = null;
        SendEmailResponse sendEmailResponse;
        try {
            template = getCustomerConfirmationEmailTemplate(submission);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (OrganizationIdEnum.G_YODER_AUDIO_EXPRESSIONS.equals(submission.getOrganizationId())) {
            sendEmailResponse = send(sesV2Client, djTemplateLocationConfig.sender(), Collections.singletonList(submission.getEmail()), djTemplateLocationConfig.sender(), template.subject(), template.body());
        } else {
            sendEmailResponse = send(sesV2Client, emailTemplateLocationConfig.sender(), Collections.singletonList(submission.getEmail()), emailTemplateLocationConfig.sender(), template.subject(), template.body());
        }
        LOGGER.info("Email sent successfully");
        return sendEmailResponse;
    }

    public static SendEmailResponse send(SesV2Client client,
                            String sender,
                            List<String> recipients,
                            String replyToAddresses,
                            String subject,
                            String bodyHTML) throws SesV2Exception {

        Destination destination = Destination.builder()
                .toAddresses(recipients)
                .build();

        Content content = Content.builder()
                .data(bodyHTML)
                .build();

        Content sub = Content.builder()
                .data(subject)
                .build();

        Body body = Body.builder()
                .html(content)
                .build();

        Message msg = Message.builder()
                .subject(sub)
                .body(body)
                .build();

        SendEmailRequest emailRequest = SendEmailRequest.builder()
                .destination(destination)
                .content(EmailContent.builder().simple(msg).build())
                .fromEmailAddress(sender)
                .replyToAddresses(replyToAddresses)
                .build();

        try {
            LOGGER.info("Attempting to send an email through Amazon SES " + "using the AWS SDK for Java...");
            return client.sendEmail(emailRequest);

        } catch (SesV2Exception e) {
            if (e.getMessage().contains("API for service 'sesv2' not yet implemented or pro feature")) {
                LOGGER.info(e.getMessage());
                return null;
            } else {
                LOGGER.error(e.awsErrorDetails().errorMessage());
                throw e;
            }
        }
    }

    public EmailTemplate getCustomerConfirmationEmailTemplate(@NotNull final Submission submission) throws IOException {
        String subject = "Thank you for contacting " + OrganizationIdEnum.getCompanyName(submission.getOrganizationId());

        InputStream is = new ClassPathResource(emailTemplateLocationConfig.confirmationToClientEmail()).getInputStream();
        String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        String body = content
            .replace("${companyName}", OrganizationIdEnum.getCompanyName(submission.getOrganizationId()))
            .replace("${name}", submission.getName())
            .replace("${phone}", submission.getPhone())
            .replace("${email}", submission.getEmail())
            .replace("${comments}", submission.getComments());

        return new EmailTemplate(subject, body);
    }

}
