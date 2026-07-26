package eshop.com.eshoporderservice.outbox;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@TestPropertySource(properties = {
        "spring.config.import=optional:consul:",
        "spring.cloud.consul.enabled=false",
        "spring.cloud.consul.discovery.enabled=false",
        "spring.cloud.consul.config.enabled=false"
})
class OutboxEventRelayIT {

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
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private OutboxEventRelay outboxEventRelay;

    @Test
    void relay_whenUnpublishedEventExists_thenPublishesToKafkaAndPersistsPublishedFlag() {
        OutboxEvent event = new OutboxEvent();
        event.setTopic("order-events");
        event.setPayload("{\"orderId\":\"order-it-1\",\"product\":\"product-it\",\"quantity\":3}");
        event.setCreatedAt(LocalDateTime.now());
        OutboxEvent saved = outboxEventRepository.save(event);

        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(kafka.getBootstrapServers(), "outbox-it-group", "true");
        DefaultKafkaConsumerFactory<String, String> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProps);
        Consumer<String, String> consumer = consumerFactory.createConsumer();
        consumer.subscribe(List.of("order-events"));

        try {
            outboxEventRelay.relay();

            ConsumerRecord<String, String> record = pollForSingleRecord(consumer, Duration.ofSeconds(15));
            assertThat(record.value()).isEqualTo(saved.getPayload());
        } finally {
            consumer.close();
        }

        OutboxEvent updated = outboxEventRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.isPublished()).isTrue();
    }

    private ConsumerRecord<String, String> pollForSingleRecord(Consumer<String, String> consumer, Duration timeout) {
        long deadline = System.currentTimeMillis() + timeout.toMillis();
        while (System.currentTimeMillis() < deadline) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
            if (!records.isEmpty()) {
                return records.iterator().next();
            }
        }
        throw new AssertionError("No record received on order-events within " + timeout);
    }
}
