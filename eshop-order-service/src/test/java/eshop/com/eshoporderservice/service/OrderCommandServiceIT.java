package eshop.com.eshoporderservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import eshop.com.eshoporderservice.event.OrderCreatedEvent;
import eshop.com.eshoporderservice.order.model.OrderCommand;
import eshop.com.eshoporderservice.order.repository.OrderCommandRepository;
import eshop.com.eshoporderservice.outbox.OutboxEvent;
import eshop.com.eshoporderservice.outbox.OutboxEventRepository;
import eshop.com.eshoporderservice.web.dto.OrderCommandCreateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@TestPropertySource(properties = {
        "spring.config.import=optional:consul:",
        "spring.cloud.consul.enabled=false",
        "spring.cloud.consul.discovery.enabled=false",
        "spring.cloud.consul.config.enabled=false"
})
class OrderCommandServiceIT {

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
        registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri", () -> "http://localhost/dummy-jwks");
    }

    @Autowired
    private OrderCommandService orderCommandService;

    @Autowired
    private OrderCommandRepository orderCommandRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createOrder_whenValidRequest_thenPersistsOrderAndOutboxEventTogether() throws Exception {
        OrderCommandCreateRequest request = new OrderCommandCreateRequest();
        request.setProduct("Laptop");
        request.setQuantity(2);

        OrderCommand saved = orderCommandService.createOrder(request);

        Optional<OrderCommand> persistedOrder = orderCommandRepository.findById(saved.getId());
        assertThat(persistedOrder).isPresent();
        assertThat(persistedOrder.get().getStatus()).isEqualTo("PENDING");

        List<OutboxEvent> outboxEvents = outboxEventRepository.findAll().stream()
                .filter(e -> e.getPayload().contains(saved.getId().toString()))
                .toList();
        assertThat(outboxEvents).singleElement().satisfies(outboxEvent -> {
            assertThat(outboxEvent.getTopic()).isEqualTo("order-events");
            assertThat(outboxEvent.isPublished()).isFalse();
        });

        OrderCreatedEvent event = objectMapper.readValue(outboxEvents.get(0).getPayload(), OrderCreatedEvent.class);
        assertThat(event.orderId()).isEqualTo(saved.getId());
        assertThat(event.productId()).isEqualTo("Laptop");
        assertThat(event.quantity()).isEqualTo(2);
    }
}
