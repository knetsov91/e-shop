package eshop.com.eshopauthservice;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class EshopAuthServiceApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(EshopAuthServiceApplication.class)
                .web(WebApplicationType.SERVLET).run(args);
    }

}
