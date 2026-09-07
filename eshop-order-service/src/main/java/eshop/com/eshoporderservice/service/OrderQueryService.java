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

    public Page<OrderQuery> getOrders(String userId, boolean isAdmin, OrderStatus status, Pageable pageable) {
        if (isAdmin) {
            return status != null
                    ? orderQueryRepository.findByStatus(status.name(), pageable)
                    : orderQueryRepository.findAll(pageable);
        }
        return status != null
                ? orderQueryRepository.findByUserIdAndStatus(userId, status.name(), pageable)
                : orderQueryRepository.findByUserId(userId, pageable);
    }

    public Optional<OrderQuery> getOrderById(String orderId, String userId, boolean isAdmin) {
        return orderQueryRepository.findById(orderId)
                .filter(order -> isAdmin || order.getUserId().equals(userId));
    }
}
