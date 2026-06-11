package eshop.com.eshoporderservice.outbox;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxEventRelayTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private OutboxEventRelay outboxEventRelay;

    @Test
    void relay_whenUnpublishedEventsExist_thenSendsEachToKafka() throws Exception {
        OutboxEvent first = new OutboxEvent();
        first.setTopic("order-events");
        first.setPayload("{\"id\":\"1\"}");

        OutboxEvent second = new OutboxEvent();
        second.setTopic("order-events");
        second.setPayload("{\"id\":\"2\"}");

        when(outboxEventRepository.findUnpublishedForUpdate()).thenReturn(List.of(first, second));
        when(kafkaTemplate.send(anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        outboxEventRelay.relay();

        verify(kafkaTemplate, times(2)).send(anyString(), anyString());
        assertThat(first.isPublished()).isTrue();
        assertThat(second.isPublished()).isTrue();
    }
}
