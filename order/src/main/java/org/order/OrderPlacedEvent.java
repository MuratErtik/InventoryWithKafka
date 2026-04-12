package org.order;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderPlacedEvent {
    private String product;

    private Double price;
}
