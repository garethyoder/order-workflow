package com.cedarmeadowmeats.orderworkflow.sendemailalert.service;


import com.cedarmeadowmeats.orderworkflow.sendemailalert.config.DjTemplateLocationConfig;
import com.cedarmeadowmeats.orderworkflow.sendemailalert.config.EmailTemplateLocationConfig;
import com.cedarmeadowmeats.orderworkflow.sendemailalert.model.EmailTemplate;
import com.cedarmeadowmeats.orderworkflow.sendemailalert.model.FormEnum;
import com.cedarmeadowmeats.orderworkflow.sendemailalert.model.OrganizationIdEnum;
import com.cedarmeadowmeats.orderworkflow.sendemailalert.model.Submission;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
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

    public SendEmailResponse sendSubmissionAlertEmail(final Submission submission) throws SesV2Exception {

        EmailTemplate template = null;
        try {
            template = getAlertEmailBody(submission);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String subject = template.subject();
        if (submission.getSpam()) {
            subject = "[SPAM DETECTED] " + subject;
        }

        SendEmailResponse sendEmailResponse;
        if (OrganizationIdEnum.G_YODER_AUDIO_EXPRESSIONS.equals(submission.getOrganizationId())) {
            sendEmailResponse = send(sesV2Client, djTemplateLocationConfig.noReplySender(), djTemplateLocationConfig.adminEmails(), submission.getEmail(), subject, template.body());
        } else {
            sendEmailResponse = send(sesV2Client, emailTemplateLocationConfig.noReplySender(), emailTemplateLocationConfig.adminEmails(), submission.getEmail(), subject, template.body());
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

    public EmailTemplate getAlertEmailBody(@NotNull final Submission submission) throws IOException {
        String subject = OrganizationIdEnum.getCompanyName(submission.getOrganizationId()) + " " +
                FormEnum.getFormName(submission.getForm()) + ": " +
                submission.getName();

        String body;
        if (OrganizationIdEnum.G_YODER_AUDIO_EXPRESSIONS.equals(submission.getOrganizationId())) {
            InputStream is = new ClassPathResource(djTemplateLocationConfig.defaultAlertEmail()).getInputStream();
            String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            body = content
                .replace("${companyName}", OrganizationIdEnum.getCompanyName(submission.getOrganizationId()))
                .replace("${name}", submission.getName())
                .replace("${phone}", submission.getPhone())
                .replace("${email}", submission.getEmail())
                .replace("${eventDate}", submission.getEventDate())
                .replace("${venue}", submission.getVenue())
                .replace("${comments}", Optional.ofNullable(submission.getComments()).orElse(""));
        } else if (FormEnum.ORDER_FORM.equals(submission.getForm())) {
            InputStream is = new ClassPathResource(emailTemplateLocationConfig.orderFormAlertEmail()).getInputStream();
            String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            body = content
                    .replace("${companyName}", OrganizationIdEnum.getCompanyName(submission.getOrganizationId()))
                    .replace("${name}", submission.getName())
                    .replace("${phone}", submission.getPhone())
                    .replace("${email}", submission.getEmail())
                    .replace("${selection}", submission.getOrderFormSelectionEnum().getValue())
                    .replace("${comments}", submission.getComments())
                    .replace("${referral}", (StringUtils.hasLength(submission.getReferral()) ? submission.getReferral() : ""))
                    .replace("${referralDisplay}", (StringUtils.hasLength(submission.getReferral()) ? "block" : "none"));
        } else {
            InputStream is = new ClassPathResource(emailTemplateLocationConfig.defaultAlertEmail()).getInputStream();
            String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            body = content
                    .replace("${companyName}", OrganizationIdEnum.getCompanyName(submission.getOrganizationId()))
                    .replace("${name}", submission.getName())
                    .replace("${phone}", submission.getPhone())
                    .replace("${email}", submission.getEmail())
                    .replace("${comments}", submission.getComments());
        }

        return new EmailTemplate(subject, body);
    }

}
