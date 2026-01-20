package com.cedarmeadowmeats.orderworkflow.createcustomerinsquare.service;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cedarmeadowmeats.orderworkflow.createcustomerinsquare.model.Submission;
import com.squareup.square.SquareClient;
import com.squareup.square.api.CustomersApi;
import com.squareup.square.api.DefaultCustomersApi;
import com.squareup.square.exceptions.ApiException;
import com.squareup.square.models.CreateCustomerRequest;
import com.squareup.square.models.CreateCustomerResponse;
import com.squareup.square.models.Customer;
import com.squareup.square.models.SearchCustomersRequest;
import com.squareup.square.models.SearchCustomersResponse;
import java.io.IOException;
import java.util.List;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SquareServiceTest {

  private Submission submission;

  @InjectMocks
  private SquareService squareService;

  @Mock
  private SquareClient squareClient;

  @Captor
  private ArgumentCaptor<SearchCustomersRequest> searchCustomersRequestArgumentCaptor;

  @Captor
  private ArgumentCaptor<CreateCustomerRequest> createCustomerRequestArgumentCaptor;


  @BeforeEach
  void setUp() {
    submission = new Submission();
    submission.setName("Jane Doe");
    submission.setEmail("client@test.com");
    submission.setPhone("717-368-2610");
    submission.setIdempotencyKey("unique_key");
    submission.setSpam(false);
  }

  @Test
  void findExistingCustomer() throws IOException, ApiException {
    SearchCustomersResponse customersResponse = mock(SearchCustomersResponse.class);

    CustomersApi mockCustomersApi = mock(DefaultCustomersApi.class);
    when(customersResponse.getCustomers()).thenReturn(List.of(new Customer.Builder().build()));
    when(mockCustomersApi.searchCustomers(searchCustomersRequestArgumentCaptor.capture())).thenReturn(customersResponse);
    when(squareClient.getCustomersApi()).thenReturn(mockCustomersApi);

    squareService.clientSubmission(submission);

    // assert Square search query contains email
    MatcherAssert.assertThat("Search query must contain email.", searchCustomersRequestArgumentCaptor.getValue().getQuery().toString(), containsString("client@test.com"));

    verify(mockCustomersApi, times(1)).searchCustomers(any());
    verify(mockCustomersApi, times(0)).createCustomer(any());
  }

  @Test
  void addNewCustomer() throws IOException, ApiException {

    SearchCustomersResponse customersResponse = mock(SearchCustomersResponse.class);

    CustomersApi mockCustomersApi = mock(DefaultCustomersApi.class);

    // Search Customers Mock
    when(customersResponse.getCustomers()).thenReturn(null);
    when(mockCustomersApi.searchCustomers(searchCustomersRequestArgumentCaptor.capture())).thenReturn(customersResponse);

    // Create Customer Mock
    CreateCustomerResponse createCustomerResponse = mock(CreateCustomerResponse.class);
    doReturn(createCustomerResponse).when(mockCustomersApi).createCustomer(createCustomerRequestArgumentCaptor.capture());
    when(squareClient.getCustomersApi()).thenReturn(mockCustomersApi);

    squareService.clientSubmission(submission);

    Assertions.assertEquals(submission.getEmail(), createCustomerRequestArgumentCaptor.getValue().getEmailAddress());
    Assertions.assertEquals(submission.getPhone(), createCustomerRequestArgumentCaptor.getValue().getPhoneNumber());
    Assertions.assertEquals("Jane", createCustomerRequestArgumentCaptor.getValue().getGivenName());
    Assertions.assertEquals("Doe", createCustomerRequestArgumentCaptor.getValue().getFamilyName());
    Assertions.assertEquals(submission.getIdempotencyKey(), createCustomerRequestArgumentCaptor.getValue().getIdempotencyKey());

    verify(mockCustomersApi, times(1)).searchCustomers(any());
    verify(mockCustomersApi, times(1)).createCustomer(any());
  }

}