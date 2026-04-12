package org.shipping;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    @KafkaListener(topics ="prod.orders.placed"/*,groupId = "shipping-group"*/)
    public void handleOrderPlacedEvent(OrderPlacedEvent event){

        System.out.println("Order Placed Event: " + event);

    }
}
