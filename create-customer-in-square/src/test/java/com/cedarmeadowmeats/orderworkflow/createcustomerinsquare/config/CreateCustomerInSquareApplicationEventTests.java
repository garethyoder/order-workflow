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
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.mockito.ArgumentCaptor;
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
    private final ArgumentCaptor<SearchCustomersRequest> searchCustomersRequestArgumentCaptor = ArgumentCaptor.forClass(SearchCustomersRequest.class);

    private final ArgumentCaptor<CreateCustomerRequest> createCustomerRequestArgumentCaptor = ArgumentCaptor.forClass(CreateCustomerRequest.class);

    @ParameterizedTest
    @Event(value = "dynamodb/order_form_event.json", type = DynamodbEvent.DynamodbStreamRecord.class)
    public void testOrderFormEventForExistingCustomer(DynamodbEvent.DynamodbStreamRecord event)
         {
        assertThat(event).isNotNull();
        SearchCustomersResponse customersResponse = mock(SearchCustomersResponse.class);

        CustomersClient mockCustomersApi = mock(CustomersClient.class);
        when(customersResponse.getCustomers()).thenReturn(Optional.of(List.of(Customer.builder().build())));
        doReturn(customersResponse).when(mockCustomersApi).search(searchCustomersRequestArgumentCaptor.capture());
        when(squareClient.customers()).thenReturn(mockCustomersApi);

        application.createCustomerInSquare().apply(List.of(event));

        // assert Square search query contains email
        MatcherAssert.assertThat("Search query must contain email.", searchCustomersRequestArgumentCaptor.getValue().getQuery().get().toString(), containsString("client@test.com"));

        verify(mockCustomersApi, times(1)).search(any(SearchCustomersRequest.class));
    }

    @ParameterizedTest
    @Event(value = "dynamodb/contact_form_event.json", type = DynamodbEvent.DynamodbStreamRecord.class)
    public void testContactFormEventForNewCustomer(DynamodbEvent.DynamodbStreamRecord event)
         {
        assertThat(event).isNotNull();
        SearchCustomersResponse customersResponse = mock(SearchCustomersResponse.class);

        CustomersClient mockCustomersApi = mock(CustomersClient.class);

        // Search Customers Mock
        when(customersResponse.getCustomers()).thenReturn(Optional.empty());
        doReturn(customersResponse).when(mockCustomersApi).search(searchCustomersRequestArgumentCaptor.capture());

        // Create Customer Mock
        CreateCustomerResponse createCustomerResponse = mock(CreateCustomerResponse.class);
        doReturn(createCustomerResponse).when(mockCustomersApi).create(createCustomerRequestArgumentCaptor.capture());
        when(squareClient.customers()).thenReturn(mockCustomersApi);

        application.createCustomerInSquare().apply(List.of(event));

        Assertions.assertEquals("client@test.com", createCustomerRequestArgumentCaptor.getValue().getEmailAddress().orElse(null));
        Assertions.assertEquals("123-456-7890", createCustomerRequestArgumentCaptor.getValue().getPhoneNumber().orElse(null));
        Assertions.assertEquals("Jane", createCustomerRequestArgumentCaptor.getValue().getGivenName().orElse(null));
        Assertions.assertEquals("Doe", createCustomerRequestArgumentCaptor.getValue().getFamilyName().orElse(null));
        Assertions.assertEquals("ec1805c55c0e3954e1512f1e00473b16", createCustomerRequestArgumentCaptor.getValue().getIdempotencyKey().orElse(null));

        verify(mockCustomersApi, times(1)).search(any(SearchCustomersRequest.class));
        verify(mockCustomersApi, times(1)).create(any(CreateCustomerRequest.class));
    }

    @ParameterizedTest
    @Event(value = "dynamodb/contact_form_event_with_spam.json", type = DynamodbEvent.DynamodbStreamRecord.class)
    public void testContactFormEventWithSpam(DynamodbEvent.DynamodbStreamRecord event)
         {
        assertThat(event).isNotNull();
        //SearchCustomersResponse customersResponse = mock(SearchCustomersResponse.class);

        CustomersClient mockCustomersApi = mock(CustomersClient.class);

        // Search Customers Mock
//        when(customersResponse.getCustomers()).thenReturn(Optional.empty());
//        doReturn(customersResponse).when(mockCustomersApi).search(searchCustomersRequestArgumentCaptor.capture());
//
//        // Create Customer Mock
//        CreateCustomerResponse createCustomerResponse = mock(CreateCustomerResponse.class);
//        doReturn(createCustomerResponse).when(mockCustomersApi).create(createCustomerRequestArgumentCaptor.capture());
//        when(squareClient.customers()).thenReturn(mockCustomersApi);

        application.createCustomerInSquare().apply(List.of(event));

//        Assertions.assertEquals("client@test.com", createCustomerRequestArgumentCaptor.getValue().getEmailAddress().orElse(null));
//        Assertions.assertEquals("123-456-7890", createCustomerRequestArgumentCaptor.getValue().getPhoneNumber().orElse(null));
//        Assertions.assertEquals("Jane", createCustomerRequestArgumentCaptor.getValue().getGivenName().orElse(null));
//        Assertions.assertEquals("Doe", createCustomerRequestArgumentCaptor.getValue().getFamilyName().orElse(null));
//        Assertions.assertEquals("ec1805c55c0e3954e1512f1e00473b16", createCustomerRequestArgumentCaptor.getValue().getIdempotencyKey().orElse(null));

        verify(mockCustomersApi, times(0)).search(any(SearchCustomersRequest.class));
        verify(mockCustomersApi, times(0)).create(any(CreateCustomerRequest.class));
    }

}
