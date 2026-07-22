package eshop.com.eshopinventoryservice.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import eshop.com.eshopinventoryservice.event.OrderCreatedEvent;
import eshop.com.eshopinventoryservice.model.Stock;
import eshop.com.eshopinventoryservice.repository.ProcessedOrderEventRepository;
import eshop.com.eshopinventoryservice.repository.StockRepository;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class OrderEventConsumerIT {

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

    @Container
    static KafkaContainer kafka = new KafkaContainer("apache/kafka-native:3.8.0");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.cloud.consul.discovery.enabled", () -> "false");
        registry.add("spring.cloud.consul.config.enabled", () -> "false");
    }

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private ProcessedOrderEventRepository processedOrderEventRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void consume_whenSameOrderEventDeliveredTwice_thenReservesStockOnceAndPublishesOnce() throws Exception {
        Stock stock = new Stock();
        stock.setProductId("product-it");
        stock.setQuantity(10);
        stockRepository.save(stock);

        UUID orderId = UUID.randomUUID();
        String message = objectMapper.writeValueAsString(new OrderCreatedEvent(orderId, "product-it", 3));

        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(kafka.getBootstrapServers(), "it-group", "true");
        DefaultKafkaConsumerFactory<String, String> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProps);
        Consumer<String, String> consumer = consumerFactory.createConsumer();
        consumer.subscribe(List.of("inventory-events"));

        try {
            kafkaTemplate.send("order-events", message);
            kafkaTemplate.send("order-events", message);

            ConsumerRecord<String, String> record = pollForSingleRecord(consumer, Duration.ofSeconds(15));
            assertThat(record.value()).contains("RESERVED");

            ConsumerRecords<String, String> unexpected = consumer.poll(Duration.ofSeconds(3));
            assertThat(unexpected.count()).isZero();
        } finally {
            consumer.close();
        }

        Stock updated = stockRepository.findByProductId("product-it").orElseThrow();
        assertThat(updated.getQuantity()).isEqualTo(7);
        assertThat(processedOrderEventRepository.existsById(orderId)).isTrue();
    }

    private ConsumerRecord<String, String> pollForSingleRecord(Consumer<String, String> consumer, Duration timeout) {
        long deadline = System.currentTimeMillis() + timeout.toMillis();
        while (System.currentTimeMillis() < deadline) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
            if (!records.isEmpty()) {
                return records.iterator().next();
            }
        }
        throw new AssertionError("No record received on inventory-events within " + timeout);
    }
}
