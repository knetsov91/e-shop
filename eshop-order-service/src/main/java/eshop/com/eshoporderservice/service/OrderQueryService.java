package eshop.com.eshoporderservice.service;

import eshop.com.eshoporderservice.order.model.OrderQuery;
import eshop.com.eshoporderservice.order.repository.OrderQueryRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class OrderQueryService {

    private final OrderQueryRepository orderQueryRepository;

    public OrderQueryService(OrderQueryRepository orderQueryRepository) {
        this.orderQueryRepository = orderQueryRepository;
    }

    public List<OrderQuery> getAllOrders() {
        return  orderQueryRepository.findAll();
    }
}
