package eshop.com.eshoporderservice.web;

import eshop.com.eshoporderservice.order.model.OrderQuery;
import eshop.com.eshoporderservice.order.model.OrderStatus;
import eshop.com.eshoporderservice.service.OrderQueryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderQueryController {

    private static final String ADMIN_AUTHORITY = "ROLE_ADMIN";

    private final OrderQueryService orderQueryService;

    public OrderQueryController(OrderQueryService orderQueryService) {
        this.orderQueryService = orderQueryService;
    }

    @GetMapping
    public ResponseEntity<Page<OrderQuery>> getAllOrders(@RequestParam(required = false) OrderStatus status,
                                                           Pageable pageable,
                                                           @AuthenticationPrincipal Jwt jwt,
                                                           Authentication authentication) {
        Page<OrderQuery> orders = orderQueryService.getOrders(jwt.getSubject(), isAdmin(authentication), status, pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderQuery> getOrderById(@PathVariable String orderId,
                                                    @AuthenticationPrincipal Jwt jwt,
                                                    Authentication authentication) {
        return orderQueryService.getOrderById(orderId, jwt.getSubject(), isAdmin(authentication))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(ADMIN_AUTHORITY));
    }
}
