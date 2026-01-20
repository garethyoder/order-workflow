package com.cedarmeadowmeats.orderworkflow.createcustomerinsquare.service;

import com.cedarmeadowmeats.orderworkflow.createcustomerinsquare.model.Submission;
import com.squareup.square.SquareClient;
import com.squareup.square.exceptions.ApiException;
import com.squareup.square.models.CreateCustomerRequest;
import com.squareup.square.models.CreateCustomerResponse;
import com.squareup.square.models.Customer;
import com.squareup.square.models.CustomerFilter;
import com.squareup.square.models.CustomerQuery;
import com.squareup.square.models.CustomerTextFilter;
import com.squareup.square.models.SearchCustomersRequest;
import java.io.IOException;
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

    public void clientSubmission(final Submission submission) throws IOException, ApiException {

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

    public void addNewCustomer(final Submission submission) throws IOException, ApiException {
        String[] names = submission.getName().split(" ");

        CreateCustomerRequest createCustomerRequest = new CreateCustomerRequest.Builder()
            .givenName(names[0])
            .familyName(names[names.length - 1])
            .emailAddress(submission.getEmail())
            .phoneNumber(submission.getPhone())
            .idempotencyKey(submission.getIdempotencyKey())
            .build();
        CreateCustomerResponse response = squareClient.getCustomersApi().createCustomer(createCustomerRequest);
        LOGGER.info("Response: {}", response.toString());
    }

    public List<Customer> getCustomersByEmail(final String email) {
        List<Customer> customers;
        try {
            SearchCustomersRequest request = new SearchCustomersRequest.Builder()
                    .query(new CustomerQuery.Builder()
                            .filter(new CustomerFilter.Builder()
                                    .emailAddress(new CustomerTextFilter.Builder()
                                            .exact(email)
                                            .build())
                                    .build())
                            .build())
                    .build();
            customers = squareClient.getCustomersApi().searchCustomers(request).getCustomers();
        } catch (ApiException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return customers;
    }
}
