package com.codecafe.cdc.consumer;

import com.codecafe.cdc.message.DebeziumMessage;
import com.codecafe.cdc.message.Operation;
import com.codecafe.cdc.message.Product;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CDCEventsConsumer {

    private final ObjectMapper objectMapper;

    public CDCEventsConsumer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = {"testDbServer_products"})
    public void onMessage(@Payload(required = false) DebeziumMessage<Product> message,
                          @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
                          @Header(KafkaHeaders.OFFSET) int offset) {
        log.info("Received message [{}] from partition-{} with offset-{}", message, partition, offset);

        if (message != null) {
            Product productBefore = objectMapper.convertValue(message.getBefore(), Product.class);
            Product productAfter = objectMapper.convertValue(message.getAfter(), Product.class);
            Operation operation = message.getOp();

            log.info("\nproductBefore : [{}]\nproductAfter : [{}]\noperation : [{}]", productBefore, productAfter, operation);
        }
    }

}
