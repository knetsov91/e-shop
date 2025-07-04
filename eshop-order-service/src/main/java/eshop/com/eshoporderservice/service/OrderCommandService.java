package eshop.com.eshoporderservice.service;

import eshop.com.eshoporderservice.order.model.OrderCommand;
import eshop.com.eshoporderservice.order.repository.OrderCommandRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderCommandService {

    private OrderCommandRepository  orderCommandRepository;

    private KafkaTemplate<String, String> kafkaTemplate;

    public OrderCommandService(OrderCommandRepository orderCommandRepository, KafkaTemplate<String, String> kafkaTemplate) {
        this.orderCommandRepository = orderCommandRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public OrderCommand createOrder(OrderCommand orderCommand) {
        OrderCommand save = orderCommandRepository.save(orderCommand);
        kafkaTemplate.send("order-events", "OrderCreated:%s".formatted(save.getId()));
        return save;
    }
}