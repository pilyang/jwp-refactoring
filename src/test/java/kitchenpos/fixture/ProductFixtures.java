package kitchenpos.fixture;

import kitchenpos.domain.Price;
import kitchenpos.domain.Product;

import java.math.BigDecimal;

public class ProductFixtures {

    public static Product PIZZA() {
        return new Product("피자", new Price(BigDecimal.valueOf(20000)));
    }
}
