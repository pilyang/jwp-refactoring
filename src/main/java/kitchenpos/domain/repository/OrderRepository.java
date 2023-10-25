package kitchenpos.domain.repository;

import kitchenpos.dao.OrderDao;
import kitchenpos.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long>, OrderDao {
}