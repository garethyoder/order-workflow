package com.cedarmeadowmeats.orderworkflow.sendemailconfirmation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.tests.annotations.Event;
import java.util.List;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.function.context.test.FunctionalSpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.SendEmailRequest;
import software.amazon.awssdk.services.sesv2.model.SendEmailResponse;

@ExtendWith(SpringExtension.class)
@FunctionalSpringBootTest
public class SendEmailConfirmationApplicationEventTest {

  @Autowired
  private SendEmailConfirmationApplication application;

  @MockitoBean
  private SesV2Client sesV2Client;
  @Captor
  private ArgumentCaptor<SendEmailRequest> sendEmailRequestArgumentCaptor;

  @BeforeEach
  void setUp() {
    SendEmailResponse sendEmailResponse = SendEmailResponse.builder().build();
    doReturn(sendEmailResponse).when(sesV2Client).sendEmail(sendEmailRequestArgumentCaptor.capture());
  }

  @ParameterizedTest
  @Event(value = "dynamodb/order_form_event.json", type = DynamodbEvent.DynamodbStreamRecord.class)
  public void testOrderFormEvent(DynamodbEvent.DynamodbStreamRecord event) {
    assertThat(event).isNotNull();
    Assertions.assertEquals("Success", application.sendEmailConfirmation().apply(List.of(event)));

    verify(sesV2Client, times(1)).sendEmail(any(SendEmailRequest.class));

    SendEmailRequest confirmationEmail = sendEmailRequestArgumentCaptor.getAllValues().getFirst();

    // Confirmation Email Assertions
    Assertions.assertEquals("client@test.com", confirmationEmail.destination().toAddresses().getFirst(), "Verify the destination email is the client.");
    Assertions.assertEquals("Gareth Yoder <gyoder@cedarmeadowmeats.com>", confirmationEmail.fromEmailAddress(), "Verify the sender email is the admin.");
    Assertions.assertEquals("Gareth Yoder <gyoder@cedarmeadowmeats.com>", confirmationEmail.replyToAddresses().getFirst(), "Verify the reply to is the admin.");
    Assertions.assertEquals("Thank you for contacting Cedar Meadow Meats", confirmationEmail.content().simple().subject().data(), "Verify the email subject line.");
    MatcherAssert.assertThat("Confirmation email body.", confirmationEmail.content().simple().body().toString(), containsString("Thank you for reaching out to Cedar Meadow Meats.  We typically respond in 1-2 business days."));

  }

  @ParameterizedTest
  @Event(value = "dynamodb/contact_form_event.json", type = DynamodbEvent.DynamodbStreamRecord.class)
  public void testContactFormEvent(DynamodbEvent.DynamodbStreamRecord event) {
    assertThat(event).isNotNull();
    Assertions.assertEquals("Success", application.sendEmailConfirmation().apply(List.of(event)));

    verify(sesV2Client, times(1)).sendEmail(any(SendEmailRequest.class));

    SendEmailRequest confirmationEmail = sendEmailRequestArgumentCaptor.getAllValues().getFirst();

    // Confirmation Email Assertions
    Assertions.assertEquals("client@test.com", confirmationEmail.destination().toAddresses().getFirst(), "Verify the destination email is the client.");
    Assertions.assertEquals("Gareth Yoder <gyoder@cedarmeadowmeats.com>", confirmationEmail.fromEmailAddress(), "Verify the sender email is the admin.");
    Assertions.assertEquals("Gareth Yoder <gyoder@cedarmeadowmeats.com>", confirmationEmail.replyToAddresses().getFirst(), "Verify the reply to is the admin.");
    Assertions.assertEquals("Thank you for contacting Cedar Meadow Naturals", confirmationEmail.content().simple().subject().data(), "Verify the email subject line.");
    MatcherAssert.assertThat("Confirmation email body.", confirmationEmail.content().simple().body().toString(), containsString("Thank you for reaching out to Cedar Meadow Naturals.  We typically respond in 1-2 business days."));

  }

  @ParameterizedTest
  @Event(value = "dynamodb/dj_form_event.json", type = DynamodbEvent.DynamodbStreamRecord.class)
  public void testDJContactFormEvent(DynamodbEvent.DynamodbStreamRecord event) {
    assertThat(event).isNotNull();
    Assertions.assertEquals("Success", application.sendEmailConfirmation().apply(List.of(event)));

    verify(sesV2Client, times(1)).sendEmail(any(SendEmailRequest.class));

    SendEmailRequest confirmationEmail = sendEmailRequestArgumentCaptor.getAllValues().getFirst();

    // Confirmation Email Assertions
    Assertions.assertEquals("client@test.com", confirmationEmail.destination().toAddresses().getFirst(), "Verify the destination email is the client.");
    Assertions.assertEquals("Gareth Yoder <gyoder@gyoderaudioexpressions.com>", confirmationEmail.fromEmailAddress(), "Verify the sender email is the admin.");
    Assertions.assertEquals("Gareth Yoder <gyoder@gyoderaudioexpressions.com>", confirmationEmail.replyToAddresses().getFirst(), "Verify the reply to is the admin.");
    Assertions.assertEquals("Thank you for contacting G Yoder Audio Expressions", confirmationEmail.content().simple().subject().data(), "Verify the email subject line.");
    MatcherAssert.assertThat("Confirmation email body.", confirmationEmail.content().simple().body().toString(), containsString("Thank you for reaching out to G Yoder Audio Expressions.  We typically respond in 1-2 business days."));

  }
}
