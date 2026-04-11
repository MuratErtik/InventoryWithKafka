package org.shipping;

import org.springframework.boot.SpringApplication;

public class TestShippingApplication {

    public static void main(String[] args) {
        SpringApplication.from(ShippingApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
