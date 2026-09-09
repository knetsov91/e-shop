package eshop.com.eshoporderservice.web;

import eshop.com.eshoporderservice.order.model.OrderQuery;
import eshop.com.eshoporderservice.order.model.OrderStatus;
import eshop.com.eshoporderservice.service.OrderQueryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderQueryController {

    private final OrderQueryService orderQueryService;

    public OrderQueryController(OrderQueryService orderQueryService) {
        this.orderQueryService = orderQueryService;
    }

    @GetMapping
    public ResponseEntity<Page<OrderQuery>> getAllOrders(@RequestParam(required = false) OrderStatus status, Pageable pageable) {
        if (status != null) {
            return ResponseEntity.ok(orderQueryService.getOrdersByStatus(status, pageable));
        }
        return ResponseEntity.ok(orderQueryService.getAllOrders(pageable));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderQuery> getOrderById(@PathVariable String orderId) {
        return orderQueryService.getOrderById(orderId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
