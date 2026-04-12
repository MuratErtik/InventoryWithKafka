package org.order;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final KafkaTemplate kafkaTemplate;



    public void placeOrder(PlaceOrderRequest request) {
        //for observability, these events should be in the same method.
        //save into db
        //publish event
        kafkaTemplate.send("prod.orders.placed","123", OrderPlacedEvent.builder()
                //key should be correlationId or like this came from db.
                .price(request.getPrice())
                .product(request.getProduct())
                .build());

        System.out.println("Order placed with " + request);
    }
}
