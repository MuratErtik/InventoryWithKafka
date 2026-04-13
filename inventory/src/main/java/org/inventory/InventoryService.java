package org.inventory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final Map<String,InventoryStatus> statues = Map.of("1",new InventoryStatus(true),"2",new InventoryStatus(false));


    public InventoryStatus getInventory(String productId) {

        return statues.getOrDefault(productId,new InventoryStatus(false));
    }
}
