package eshop.com.eshoporderservice.order.repository;

import eshop.com.eshoporderservice.order.model.OrderQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderQueryRepository extends MongoRepository<OrderQuery, String> {

    Page<OrderQuery> findByStatus(String status, Pageable pageable);

    Page<OrderQuery> findByUserId(String userId, Pageable pageable);

    Page<OrderQuery> findByUserIdAndStatus(String userId, String status, Pageable pageable);
}
