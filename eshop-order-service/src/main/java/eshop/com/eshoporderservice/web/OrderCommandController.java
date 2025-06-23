package eshop.com.eshoporderservice.web;

import eshop.com.eshoporderservice.order.model.OrderCommand;
import eshop.com.eshoporderservice.service.OrderCommandService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderCommandController {

    private OrderCommandService  orderCommandService;

    public OrderCommandController(OrderCommandService orderCommandService) {
        this.orderCommandService = orderCommandService;
    }

    @PostMapping
    public ResponseEntity<OrderCommand> createOrderCommand(@RequestBody  OrderCommand orderCommand) {
        OrderCommand order = orderCommandService.createOrder(orderCommand);
        return ResponseEntity.ok().body(order);
    }
}
