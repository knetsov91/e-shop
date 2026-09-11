package eshop.com.eshoporderservice.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import eshop.com.eshoporderservice.event.InventoryEvent;
import eshop.com.eshoporderservice.order.model.OrderCommand;
import eshop.com.eshoporderservice.order.model.OrderStatus;
import eshop.com.eshoporderservice.order.repository.OrderCommandRepository;
import eshop.com.eshoporderservice.order.repository.OrderQueryRepository;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@TestPropertySource(properties = {
        "spring.config.import=optional:consul:",
        "spring.cloud.consul.enabled=false",
        "spring.cloud.consul.discovery.enabled=false",
        "spring.cloud.consul.config.enabled=false",
        "sentry.dsn="
})
class InventoryEventConsumerIT {

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

    @Container
    static KafkaContainer kafka = new KafkaContainer("apache/kafka-native:3.8.0");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri", () -> "http://localhost/dummy-jwks");
    }

    @Autowired
    private OrderCommandRepository orderCommandRepository;

    @MockitoBean
    private OrderQueryRepository orderQueryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void consume_whenReservedEventReceived_thenUpdatesOrderStatusInDatabase() throws Exception {
        OrderCommand order = new OrderCommand();
        order.setProduct("product-1");
        order.setQuantity(3);
        order.setAmount(BigDecimal.valueOf(99.99));
        order.setStatus(OrderStatus.PENDING);
        order = orderCommandRepository.save(order);

        InventoryEvent event = new InventoryEvent(order.getId(), "product-1", "RESERVED");
        String message = objectMapper.writeValueAsString(event);

        Properties producerProps = new Properties();
        producerProps.put("bootstrap.servers", kafka.getBootstrapServers());
        producerProps.put("key.serializer", StringSerializer.class.getName());
        producerProps.put("value.serializer", StringSerializer.class.getName());

        try (Producer<String, String> producer = new KafkaProducer<>(producerProps)) {
            producer.send(new ProducerRecord<>("inventory-events", message)).get();
        }

        UUID orderId = order.getId();
        OrderCommand updated = pollForStatus(orderId, OrderStatus.CONFIRMED, Duration.ofSeconds(15));
        assertThat(updated.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    private OrderCommand pollForStatus(UUID orderId, OrderStatus expectedStatus, Duration timeout) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeout.toMillis();
        while (System.currentTimeMillis() < deadline) {
            Optional<OrderCommand> found = orderCommandRepository.findById(orderId);
            if (found.isPresent() && expectedStatus.equals(found.get().getStatus())) {
                return found.get();
            }
            Thread.sleep(500);
        }
        throw new AssertionError("Order " + orderId + " did not reach status " + expectedStatus + " within " + timeout);
    }
}
