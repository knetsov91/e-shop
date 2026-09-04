package eshop.com.eshoporderservice.web;

import eshop.com.eshoporderservice.order.model.OrderQuery;
import eshop.com.eshoporderservice.service.OrderQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderQueryController {

    private final OrderQueryService orderQueryService;

    public OrderQueryController(OrderQueryService orderQueryService) {
        this.orderQueryService = orderQueryService;
    }

    @GetMapping
    public ResponseEntity<List<OrderQuery>> getAllOrders() {
        return ResponseEntity.ok(orderQueryService.getAllOrders());
    }
}
