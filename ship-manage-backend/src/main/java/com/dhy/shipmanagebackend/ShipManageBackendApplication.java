package com.dhy.shipmanagebackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class ShipManageBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShipManageBackendApplication.class, args);
    }

}
