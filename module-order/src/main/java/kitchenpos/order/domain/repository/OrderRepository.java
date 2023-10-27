package kitchenpos.order.domain.repository;

import kitchenpos.order.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    boolean existsByOrderTableIdInAndOrderStatusIn(List<Long> orderTableIds, List<String> orderStatus);

    boolean existsByOrderTableIdAndOrderStatusIn(Long orderTableId, List<String> list);
}
