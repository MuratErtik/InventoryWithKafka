package org.order;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final KafkaTemplate kafkaTemplate;

    private final OrderRepository orderRepository;

    private final InventoryClient inventoryClient;


    public void placeOrder(PlaceOrderRequest request) {

        InventoryStatus inventoryStatus = inventoryClient.exists(request.getProduct());
        System.out.println(inventoryStatus);

        if (!inventoryStatus.getExists()){
            throw new RuntimeException("Product not found");

        }
        //for observability, these events should be in the same method.(inserting db and publishing the event)
        //save into db
        Order order = Order.builder()
                .price(request.getPrice())
                .product(request.getProduct())
                .status("PLACED")
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

    @KafkaListener(topics = "prod.orders.shipped",groupId = "order-group")
    public void handleOrderShippedEvent(String orderId) {

        Order order = orderRepository.findById(Long.valueOf(orderId)).orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus("SHIPPED");

        orderRepository.save(order);

    }
}
