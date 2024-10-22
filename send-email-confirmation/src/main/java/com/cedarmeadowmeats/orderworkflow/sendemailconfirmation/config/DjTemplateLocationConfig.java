package com.cedarmeadowmeats.orderworkflow.sendemailconfirmation.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "send-email-confirmation.dj-email")
public record DjTemplateLocationConfig(String orderFormAlertEmail,
                                       String confirmationToClientEmail,
                                       String defaultAlertEmail,
                                       String sender,
                                       String noReplySender,
                                       List<String> adminEmails) {
}
