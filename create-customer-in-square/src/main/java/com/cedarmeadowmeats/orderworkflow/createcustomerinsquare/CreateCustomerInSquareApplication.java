package com.cedarmeadowmeats.orderworkflow.createcustomerinsquare;

import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;
import com.cedarmeadowmeats.orderworkflow.createcustomerinsquare.model.FormEnum;
import com.cedarmeadowmeats.orderworkflow.createcustomerinsquare.model.OrderFormSelectionEnum;
import com.cedarmeadowmeats.orderworkflow.createcustomerinsquare.model.OrganizationIdEnum;
import com.cedarmeadowmeats.orderworkflow.createcustomerinsquare.model.Submission;
import com.cedarmeadowmeats.orderworkflow.createcustomerinsquare.service.SquareService;
import com.squareup.square.exceptions.ApiException;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CreateCustomerInSquareApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final SquareService squareService;

    public CreateCustomerInSquareApplication(SquareService squareService) {
        this.squareService = squareService;
    }

    public static void main(String[] args) {
        SpringApplication.run(CreateCustomerInSquareApplication.class, args);
    }

    @Bean
    public Function<List<DynamodbEvent.DynamodbStreamRecord>, String> createCustomerInSquare() {
        return value -> {
            LOGGER.info("Printing Event:\n {}", value);
            value.forEach(r -> {
                Submission submission = new Submission();

                submission.setName(nullCheck(r.getDynamodb().getNewImage().get("name")));
                submission.setEmail(nullCheck(r.getDynamodb().getNewImage().get("email")));
                submission.setPhone(nullCheck(r.getDynamodb().getNewImage().get("phone")));
                submission.setComments(nullCheck(r.getDynamodb().getNewImage().get("comments")));
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

                submission.setIdempotencyKey(r.getEventID());

              try {
                squareService.clientSubmission(submission);
              } catch (IOException | ApiException e) {
                throw new RuntimeException(e);
              }
            });

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
