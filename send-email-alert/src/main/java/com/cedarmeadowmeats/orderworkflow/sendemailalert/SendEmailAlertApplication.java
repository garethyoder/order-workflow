package com.cedarmeadowmeats.orderworkflow.sendemailalert;

import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;
import com.cedarmeadowmeats.orderworkflow.sendemailalert.model.FormEnum;
import com.cedarmeadowmeats.orderworkflow.sendemailalert.model.OrderFormSelectionEnum;
import com.cedarmeadowmeats.orderworkflow.sendemailalert.model.OrganizationIdEnum;
import com.cedarmeadowmeats.orderworkflow.sendemailalert.model.Submission;
import com.cedarmeadowmeats.orderworkflow.sendemailalert.service.EmailService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.invoke.MethodHandles;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.services.sesv2.model.SendEmailResponse;

@SpringBootApplication
public class SendEmailAlertApplication {

    private final EmailService emailService;

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public SendEmailAlertApplication(EmailService emailService) {
    this.emailService = emailService;
  }

  public static void main(String[] args) {
        SpringApplication.run(SendEmailAlertApplication.class, args);
        LOGGER.info("Application started");
  }

    @Bean
    public Function<List<DynamodbEvent.DynamodbStreamRecord>, String> sendEmailAlert() {
        return value -> {
            LOGGER.info("Printing Event:\n {}", value);
            value.forEach(r -> {


                Submission submission = new Submission();

                submission.setName(nullCheck(r.getDynamodb().getNewImage().get("name")));
                submission.setEmail(nullCheck(r.getDynamodb().getNewImage().get("email")));
                submission.setPhone(nullCheck(r.getDynamodb().getNewImage().get("phone")));
                submission.setComments(nullCheck(r.getDynamodb().getNewImage().get("comments")));
                submission.setReferral(nullCheck(r.getDynamodb().getNewImage().get("referral")));
                submission.setEventDate(nullCheck(r.getDynamodb().getNewImage().get("eventDate")));
                submission.setVenue(nullCheck(r.getDynamodb().getNewImage().get("venue")));
                submission.setOrganizationId(
                    nullCheckOrganizationIdEnum(r.getDynamodb().getNewImage().get("organizationId"))
                );
                submission.setForm(
                    nullCheckFormEnum(r.getDynamodb().getNewImage().get("form"))
                );
                submission.setOrderFormSelectionEnum(
                    nullCheckOrderFormSelectionEnum(r.getDynamodb().getNewImage().get("selection"))
                );
                submission.setCreatedDate(
                    nullCheckZonedDateTime(r.getDynamodb().getNewImage().get("createdDate"))
                );
                submission.setLastUpdatedDate(
                    nullCheckZonedDateTime(r.getDynamodb().getNewImage().get("lastUpdatedDate"))
                );
                submission.setVersion(
                    Integer.valueOf(r.getDynamodb().getNewImage().get("version").getN())
                );
                submission.setSpam(
                    nullCheckBoolean(r.getDynamodb().getNewImage().get("isSpam"))
                );

                SendEmailResponse response = emailService.sendSubmissionAlertEmail(submission);
                LOGGER.info("Email sent successfully: {}", response.toString());
            });

            return "Success";
        };
    }

  @Bean
  public Function<String, String> localTesting() {
    return value -> {
      LOGGER.info("Printing Event:\n {}", value);
      ObjectMapper mapper = new ObjectMapper();
      Submission submission = null;
      try {
        submission = mapper.readValue(value, Submission.class);
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
      LOGGER.info("Deserialized:\n {}", submission.toString());
      SendEmailResponse response = emailService.sendSubmissionAlertEmail(submission);
      LOGGER.info("Email sent successfully: {}", response.toString());

      return "Success";
    };
  }

    private String nullCheck(AttributeValue attributeValue) {
        return attributeValue == null ? null : attributeValue.getS();
    }

    private FormEnum nullCheckFormEnum(AttributeValue attributeValue) {
        return attributeValue == null ? null : FormEnum.valueOf(attributeValue.getS());
    }

    private OrganizationIdEnum nullCheckOrganizationIdEnum(AttributeValue attributeValue) {
        return attributeValue == null ? null : OrganizationIdEnum.valueOf(attributeValue.getS());
    }

    private OrderFormSelectionEnum nullCheckOrderFormSelectionEnum(AttributeValue attributeValue) {
        return attributeValue == null ? null : OrderFormSelectionEnum.valueOf(attributeValue.getS());
    }

    private ZonedDateTime nullCheckZonedDateTime(AttributeValue attributeValue) {
        return attributeValue == null ? null : ZonedDateTime.parse(attributeValue.getS());
    }

    private Boolean nullCheckBoolean(AttributeValue attributeValue) {
      return attributeValue == null ? null : attributeValue.getBOOL();
    }

}
