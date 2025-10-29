package eshop.com.eshopauthservice.config;

import org.springframework.boot.actuate.web.exchanges.InMemoryHttpExchangeRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {

    @Bean
    public InMemoryHttpExchangeRepository inMemoryHttpExchangeRepository() {
        return new InMemoryHttpExchangeRepository();
    }
}
