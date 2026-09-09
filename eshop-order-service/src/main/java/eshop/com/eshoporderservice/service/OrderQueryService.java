package eshop.com.eshoporderservice.service;

import eshop.com.eshoporderservice.order.model.OrderQuery;
import eshop.com.eshoporderservice.order.model.OrderStatus;
import eshop.com.eshoporderservice.order.repository.OrderQueryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class OrderQueryService {

    private final OrderQueryRepository orderQueryRepository;

    public OrderQueryService(OrderQueryRepository orderQueryRepository) {
        this.orderQueryRepository = orderQueryRepository;
    }

    public Page<OrderQuery> getAllOrders(Pageable pageable) {
        return orderQueryRepository.findAll(pageable);
    }

    public Optional<OrderQuery> getOrderById(String orderId) {
        return orderQueryRepository.findById(orderId);
    }

    public Page<OrderQuery> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        return orderQueryRepository.findByStatus(status.name(), pageable);
    }
}
