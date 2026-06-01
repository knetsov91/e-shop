package eshop.com.eshopinventoryservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EshopInventoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EshopInventoryServiceApplication.class, args);
    }
}
