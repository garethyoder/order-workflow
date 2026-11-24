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
import com.squareup.square.CustomersClient;
import com.squareup.square.types.CreateCustomerRequest;
import com.squareup.square.types.CreateCustomerResponse;
import com.squareup.square.types.Customer;
import com.squareup.square.types.SearchCustomersRequest;
import com.squareup.square.types.SearchCustomersResponse;
import java.util.List;
import java.util.Optional;
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
  void findExistingCustomer()  {
    SearchCustomersResponse customersResponse = mock(SearchCustomersResponse.class);

    CustomersClient mockCustomersApi = mock(CustomersClient.class);
    when(customersResponse.getCustomers()).thenReturn(Optional.of(List.of(Customer.builder().build())));
    when(mockCustomersApi.search(searchCustomersRequestArgumentCaptor.capture())).thenReturn(customersResponse);
    when(squareClient.customers()).thenReturn(mockCustomersApi);

    squareService.clientSubmission(submission);

    // assert Square search query contains email
    MatcherAssert.assertThat("Search query must contain email.", searchCustomersRequestArgumentCaptor.getValue().getQuery().get().toString(), containsString("client@test.com"));

    verify(mockCustomersApi, times(1)).search(any(SearchCustomersRequest.class));
    verify(mockCustomersApi, times(0)).create(any(CreateCustomerRequest.class));
  }

  @Test
  void addNewCustomer()  {

    SearchCustomersResponse customersResponse = mock(SearchCustomersResponse.class);

    CustomersClient mockCustomersApi = mock(CustomersClient.class);

    // Search Customers Mock
    when(customersResponse.getCustomers()).thenReturn(Optional.empty());
    when(mockCustomersApi.search(searchCustomersRequestArgumentCaptor.capture())).thenReturn(customersResponse);

    // Create Customer Mock
    CreateCustomerResponse createCustomerResponse = mock(CreateCustomerResponse.class);
    doReturn(createCustomerResponse).when(mockCustomersApi).create(createCustomerRequestArgumentCaptor.capture());
    when(squareClient.customers()).thenReturn(mockCustomersApi);

    squareService.clientSubmission(submission);

    Assertions.assertEquals(submission.getEmail(), createCustomerRequestArgumentCaptor.getValue().getEmailAddress().orElse(null));
    Assertions.assertEquals(submission.getPhone(), createCustomerRequestArgumentCaptor.getValue().getPhoneNumber().orElse(null));
    Assertions.assertEquals("Jane", createCustomerRequestArgumentCaptor.getValue().getGivenName().orElse(null));
    Assertions.assertEquals("Doe", createCustomerRequestArgumentCaptor.getValue().getFamilyName().orElse(null));
    Assertions.assertEquals(submission.getIdempotencyKey(), createCustomerRequestArgumentCaptor.getValue().getIdempotencyKey().orElse(null));

    verify(mockCustomersApi, times(1)).search(any(SearchCustomersRequest.class));
    verify(mockCustomersApi, times(1)).create(any(CreateCustomerRequest.class));
  }

}