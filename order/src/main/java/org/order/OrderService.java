package org.order;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final KafkaTemplate kafkaTemplate;

    private final OrderRepository orderRepository;



    public void placeOrder(PlaceOrderRequest request) {
        //for observability, these events should be in the same method.(inserting db and publishing the event)
        //save into db
        Order order = Order.builder()
                .price(request.getPrice())
                .product(request.getProduct())
                .build();
        orderRepository.save(order);
        //publish event
        kafkaTemplate.send("prod.orders.placed",order.getId().toString(), OrderPlacedEvent.builder()
                //key should be correlationId or like this came from db.
                .price(request.getPrice())
                .orderId(order.getId())
                .product(request.getProduct())
                .build());

        System.out.println("Order placed with " + request);
    }
}
