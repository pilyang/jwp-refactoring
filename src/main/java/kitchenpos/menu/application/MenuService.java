package kitchenpos.menu.application;

import kitchenpos.common.domain.Price;
import kitchenpos.menu.application.dto.MenuCreateRequest;
import kitchenpos.menu.application.dto.MenuCreateRequest.MenuProductRequest;
import kitchenpos.menu.application.dto.MenuResponse;
import kitchenpos.menu.domain.Menu;
import kitchenpos.menu.domain.MenuGroup;
import kitchenpos.menu.domain.repository.MenuGroupRepository;
import kitchenpos.menu.domain.repository.MenuRepository;
import kitchenpos.product.domain.Product;
import kitchenpos.product.domain.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MenuService {
    private final MenuRepository menuRepository;
    private final MenuGroupRepository menuGroupRepository;
    private final ProductRepository productRepository;

    public MenuService(
            final MenuRepository menuRepository,
            final MenuGroupRepository menuGroupRepository,
            final ProductRepository productRepository
    ) {
        this.menuRepository = menuRepository;
        this.menuGroupRepository = menuGroupRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public MenuResponse create(final MenuCreateRequest request) {
        final String menuName = request.getName();
        final Price menuPrice = new Price(request.getPrice());
        final MenuGroup menuGroup = menuGroupRepository.findById(request.getMenuGroupId())
                .orElseThrow(IllegalArgumentException::new);

        final Menu.MenuFactory menuFactory = new Menu.MenuFactory(menuName, menuPrice, menuGroup);

        final List<MenuProductRequest> menuProducts = request.getMenuProducts();
        for (final MenuProductRequest menuProductRequest : menuProducts) {
            final Product product = productRepository.findById(menuProductRequest.getProductId())
                    .orElseThrow(IllegalArgumentException::new);
            menuFactory.addProduct(product, menuProductRequest.getQuantity());
        }

        final Menu menu = menuFactory.create();
        return MenuResponse.from(menuRepository.save(menu));
    }

    public List<MenuResponse> list() {
        final List<Menu> menus = menuRepository.findAll();

        return menus.stream()
                .map(MenuResponse::from)
                .collect(Collectors.toUnmodifiableList());
    }
}
