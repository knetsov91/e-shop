package eshop.com.eshoporderservice.service;

import eshop.com.eshoporderservice.order.model.OrderQuery;
import eshop.com.eshoporderservice.order.repository.OrderQueryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

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
    void getOrders_whenAdminAndNoStatusFilter_thenReturnsAllOrders() {
        OrderQuery order = new OrderQuery("order-1", "user-1", "Laptop", 2, "PENDING");

        when(orderQueryRepository.findAll(Pageable.unpaged())).thenReturn(new PageImpl<>(List.of(order)));

        Page<OrderQuery> result = orderQueryService.getOrders("admin-1", true, null, Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getOrderId()).isEqualTo("order-1");
        assertThat(result.getContent().get(0).getProduct()).isEqualTo("Laptop");
    }

    @Test
    void getOrders_whenNotAdminAndNoStatusFilter_thenReturnsOnlyOwnOrders() {
        OrderQuery order = new OrderQuery("order-1", "user-1", "Laptop", 2, "PENDING");

        when(orderQueryRepository.findByUserId("user-1", Pageable.unpaged())).thenReturn(new PageImpl<>(List.of(order)));

        Page<OrderQuery> result = orderQueryService.getOrders("user-1", false, null, Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUserId()).isEqualTo("user-1");
    }
}
