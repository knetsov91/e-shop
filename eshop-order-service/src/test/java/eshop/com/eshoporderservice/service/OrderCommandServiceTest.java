package eshop.com.eshoporderservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import eshop.com.eshoporderservice.order.model.OrderCommand;
import eshop.com.eshoporderservice.order.model.OrderStatus;
import eshop.com.eshoporderservice.order.repository.OrderCommandRepository;
import eshop.com.eshoporderservice.outbox.OutboxEvent;
import eshop.com.eshoporderservice.outbox.OutboxEventRepository;
import eshop.com.eshoporderservice.web.dto.OrderCommandCreateRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderCommandServiceTest {

    @Mock
    private OrderCommandRepository orderCommandRepository;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Spy
    private ObjectMapper objectMapper;

    @InjectMocks
    private OrderCommandService orderCommandService;

    @Test
    void createOrder_whenValidRequest_thenSavesOrderWithPendingStatus() {
        OrderCommandCreateRequest request = new OrderCommandCreateRequest();
        request.setProduct("Laptop");
        request.setQuantity(2);
        request.setAmount(BigDecimal.valueOf(999.99));

        OrderCommand saved = new OrderCommand();
        saved.setProduct("Laptop");
        saved.setQuantity(2);
        saved.setAmount(BigDecimal.valueOf(999.99));
        saved.setStatus(OrderStatus.PENDING);

        when(orderCommandRepository.save(any(OrderCommand.class))).thenReturn(saved);

        OrderCommand result = orderCommandService.createOrder(request);

        assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(result.getProduct()).isEqualTo("Laptop");
        assertThat(result.getQuantity()).isEqualTo(2);
    }

    @Test
    void createOrder_whenValidRequest_thenSavesOutboxEventWithOrderId() {
        OrderCommandCreateRequest request = new OrderCommandCreateRequest();
        request.setProduct("Laptop");
        request.setQuantity(1);
        request.setAmount(BigDecimal.valueOf(499.99));

        UUID orderId = UUID.randomUUID();
        OrderCommand saved = new OrderCommand();
        saved.setId(orderId);
        saved.setAmount(BigDecimal.valueOf(499.99));
        saved.setStatus(OrderStatus.PENDING);

        when(orderCommandRepository.save(any(OrderCommand.class))).thenReturn(saved);

        orderCommandService.createOrder(request);

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository).save(captor.capture());
        assertThat(captor.getValue().getTopic()).isEqualTo("payment-requests");
        assertThat(captor.getValue().getPayload()).contains(orderId.toString());
    }
}
