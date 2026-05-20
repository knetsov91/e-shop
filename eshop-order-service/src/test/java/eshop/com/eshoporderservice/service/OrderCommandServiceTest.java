package eshop.com.eshoporderservice.service;

import eshop.com.eshoporderservice.order.model.OrderCommand;
import eshop.com.eshoporderservice.order.repository.OrderCommandRepository;
import eshop.com.eshoporderservice.web.dto.OrderCommandCreateRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderCommandServiceTest {

    @Mock
    private OrderCommandRepository orderCommandRepository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private OrderCommandService orderCommandService;

    @Test
    void createOrder_whenValidRequest_thenSavesOrderWithPendingStatus() {
        OrderCommandCreateRequest request = new OrderCommandCreateRequest();
        request.setProduct("Laptop");
        request.setQuantity(2);

        OrderCommand saved = new OrderCommand();
        saved.setProduct("Laptop");
        saved.setQuantity(2);
        saved.setStatus("PENDING");

        when(orderCommandRepository.save(any(OrderCommand.class))).thenReturn(saved);

        OrderCommand result = orderCommandService.createOrder(request);

        assertThat(result.getStatus()).isEqualTo("PENDING");
        assertThat(result.getProduct()).isEqualTo("Laptop");
        assertThat(result.getQuantity()).isEqualTo(2);
    }
}
