package eshop.com.eshopinventoryservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "processed_order_events")
@NoArgsConstructor
public class ProcessedOrderEvent {

    @Id
    private UUID orderId;

    public ProcessedOrderEvent(UUID orderId) {
        this.orderId = orderId;
    }

    public UUID getOrderId() {
        return orderId;
    }
}
