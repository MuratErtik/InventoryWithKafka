package org.order;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(url = "http://localhost:8093",name = "inventory")
public interface InventoryClient {

    @GetMapping("/inventory")
    InventoryStatus exists(@RequestParam String productId);


}
