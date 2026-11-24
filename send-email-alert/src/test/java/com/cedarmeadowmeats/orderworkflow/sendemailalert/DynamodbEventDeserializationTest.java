package com.cedarmeadowmeats.orderworkflow.sendemailalert;

import static org.assertj.core.api.Assertions.assertThat;

import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.function.json.JsonMapper;
import org.springframework.core.ParameterizedTypeReference;

/**
 * Lambda delivers DynamoDB stream records as raw JSON with capitalized keys (NewImage, Keys).
 * Spring Cloud Function must map them onto the AWS event model.
 */
@SpringBootTest(classes = SendEmailAlertApplication.class)
class DynamodbEventDeserializationTest {

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    void deserializesCapitalizedDynamoDbKeys() throws Exception {
        String json = "[" + Files.readString(Path.of("src/test/resources/dynamodb/order_form_event.json")) + "]";

        List<DynamodbEvent.DynamodbStreamRecord> records = jsonMapper.fromJson(json,
            new ParameterizedTypeReference<List<DynamodbEvent.DynamodbStreamRecord>>() {}.getType());

        assertThat(records).hasSize(1);
        assertThat(records.get(0).getDynamodb().getNewImage()).containsKey("email");
        assertThat(records.get(0).getDynamodb().getKeys()).containsKey("email");
    }
}
