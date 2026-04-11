package org.order;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderController {



    @PostMapping("/orders")
    public void  placeOrder(@RequestBody PlaceOrderRequest request){

    }

}
