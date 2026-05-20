package eshop.com.eshoporderservice.service;

import eshop.com.eshoporderservice.order.model.OrderQuery;
import eshop.com.eshoporderservice.order.repository.OrderQueryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderQueryServiceTest {

    @Mock
    private OrderQueryRepository orderQueryRepository;

    @InjectMocks
    private OrderQueryService orderQueryService;

    @Test
    void getAllOrders_whenOrdersExist_thenReturnsAllOrders() {
        OrderQuery order = new OrderQuery("order-1", "Laptop", 2, "PENDING");

        when(orderQueryRepository.findAll()).thenReturn(List.of(order));

        List<OrderQuery> result = orderQueryService.getAllOrders();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOrderId()).isEqualTo("order-1");
        assertThat(result.get(0).getProduct()).isEqualTo("Laptop");
    }
}
