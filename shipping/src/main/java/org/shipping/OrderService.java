package org.shipping;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final KafkaTemplate kafkaTemplate;

    private final ShippingRepository shippingRepository;

    @KafkaListener(topics ="prod.orders.placed"/*,groupId = "shipping-group"*/)
    public void handleOrderPlacedEvent(OrderPlacedEvent event){

        Shipping shipping = Shipping.builder()
                .orderId(event.getOrderId())
                .build();

        shippingRepository.save(shipping);

        kafkaTemplate.send("prod.orders.shipped",shipping.getOrderId().toString());

        System.out.println("Order Placed Event: " + event);

    }
}
