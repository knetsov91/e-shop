package eshop.com.eshoporderservice.order.repository;

import eshop.com.eshoporderservice.order.model.OrderQuery;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderQueryRepository extends MongoRepository<OrderQuery, String> {
}
