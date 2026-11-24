package com.cedarmeadowmeats.orderworkflow.createcustomerinsquare.service;

import com.cedarmeadowmeats.orderworkflow.createcustomerinsquare.model.Submission;
import com.squareup.square.SquareClient;
import com.squareup.square.core.SquareApiException;
import com.squareup.square.types.CreateCustomerRequest;
import com.squareup.square.types.CreateCustomerResponse;
import com.squareup.square.types.Customer;
import com.squareup.square.types.CustomerFilter;
import com.squareup.square.types.CustomerQuery;
import com.squareup.square.types.CustomerTextFilter;
import com.squareup.square.types.SearchCustomersRequest;
import java.lang.invoke.MethodHandles;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class SquareService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final SquareClient squareClient;

    public SquareService(final SquareClient squareClient) {
        this.squareClient = squareClient;
    }

    public void clientSubmission(final Submission submission) {

        if (!submission.getSpam()) {
            List<Customer> customers = getCustomersByEmail(submission.getEmail());

            if (CollectionUtils.isEmpty(customers)) {
                LOGGER.info("Customer does not exist in Square.  Creating a new customer record.");
                addNewCustomer(submission);
            } else {
                customers.forEach(customer -> {
                    LOGGER.info("customer exists: {}", customer.toString());
                });
            }
        } else {
            LOGGER.info("Submission marked as spam.  Not creating customer in Square.");
        }

    }

    public void addNewCustomer(final Submission submission) {
        String[] names = submission.getName().split(" ");

        CreateCustomerRequest createCustomerRequest = CreateCustomerRequest.builder()
            .givenName(names[0])
            .familyName(names[names.length - 1])
            .emailAddress(submission.getEmail())
            .phoneNumber(submission.getPhone())
            .idempotencyKey(submission.getIdempotencyKey())
            .build();
        CreateCustomerResponse response = squareClient.customers().create(createCustomerRequest);
        LOGGER.info("Response: {}", response.toString());
    }

    public List<Customer> getCustomersByEmail(final String email) {
        List<Customer> customers;
        try {
            SearchCustomersRequest request = SearchCustomersRequest.builder()
                    .query(CustomerQuery.builder()
                            .filter(CustomerFilter.builder()
                                    .emailAddress(CustomerTextFilter.builder()
                                            .exact(email)
                                            .build())
                                    .build())
                            .build())
                    .build();
            customers = squareClient.customers().search(request).getCustomers().orElse(List.of());
        } catch (SquareApiException e) {
            throw new RuntimeException(e);
        }

        return customers;
    }
}
