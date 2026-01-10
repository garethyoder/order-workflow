package com.cedarmeadowmeats.orderworkflow.createcustomerinsquare.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.tests.annotations.Event;
import com.cedarmeadowmeats.orderworkflow.createcustomerinsquare.CreateCustomerInSquareApplication;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.function.context.test.FunctionalSpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@FunctionalSpringBootTest
class CreateCustomerInSquareApplicationEventTests {

    @Autowired
    private CreateCustomerInSquareApplication application;

    @MockitoBean
    private SquareClient squareClient;
    @Captor
    private ArgumentCaptor<SearchCustomersRequest> searchCustomersRequestArgumentCaptor;

    @Captor
    private ArgumentCaptor<CreateCustomerRequest> createCustomerRequestArgumentCaptor;

    @ParameterizedTest
    @Event(value = "dynamodb/order_form_event.json", type = DynamodbEvent.DynamodbStreamRecord.class)
    public void testOrderFormEventForExistingCustomer(DynamodbEvent.DynamodbStreamRecord event)
        throws IOException, ApiException {
        assertThat(event).isNotNull();
        SearchCustomersResponse customersResponse = mock(SearchCustomersResponse.class);

        CustomersApi mockCustomersApi = mock(DefaultCustomersApi.class);
        when(customersResponse.getCustomers()).thenReturn(List.of(new Customer.Builder().build()));
        doReturn(customersResponse).when(mockCustomersApi).searchCustomers(searchCustomersRequestArgumentCaptor.capture());
        when(squareClient.getCustomersApi()).thenReturn(mockCustomersApi);

        application.createCustomerInSquare().apply(List.of(event));

        // assert Square search query contains email
        MatcherAssert.assertThat("Search query must contain email.", searchCustomersRequestArgumentCaptor.getValue().getQuery().toString(), containsString("client@test.com"));

        verify(mockCustomersApi, times(1)).searchCustomers(any());
    }

    @ParameterizedTest
    @Event(value = "dynamodb/contact_form_event.json", type = DynamodbEvent.DynamodbStreamRecord.class)
    public void testContactFormEventForNewCustomer(DynamodbEvent.DynamodbStreamRecord event)
        throws IOException, ApiException {
        assertThat(event).isNotNull();
        SearchCustomersResponse customersResponse = mock(SearchCustomersResponse.class);

        CustomersApi mockCustomersApi = mock(DefaultCustomersApi.class);

        // Search Customers Mock
        when(customersResponse.getCustomers()).thenReturn(null);
        doReturn(customersResponse).when(mockCustomersApi).searchCustomers(searchCustomersRequestArgumentCaptor.capture());

        // Create Customer Mock
        CreateCustomerResponse createCustomerResponse = mock(CreateCustomerResponse.class);
        doReturn(createCustomerResponse).when(mockCustomersApi).createCustomer(createCustomerRequestArgumentCaptor.capture());
        when(squareClient.getCustomersApi()).thenReturn(mockCustomersApi);

        application.createCustomerInSquare().apply(List.of(event));

        Assertions.assertEquals("client@test.com", createCustomerRequestArgumentCaptor.getValue().getEmailAddress());
        Assertions.assertEquals("123-456-7890", createCustomerRequestArgumentCaptor.getValue().getPhoneNumber());
        Assertions.assertEquals("Jane", createCustomerRequestArgumentCaptor.getValue().getGivenName());
        Assertions.assertEquals("Doe", createCustomerRequestArgumentCaptor.getValue().getFamilyName());
        Assertions.assertEquals("ec1805c55c0e3954e1512f1e00473b16", createCustomerRequestArgumentCaptor.getValue().getIdempotencyKey());

        verify(mockCustomersApi, times(1)).searchCustomers(any());
        verify(mockCustomersApi, times(1)).createCustomer(any());
    }

}
