package kitchenpos.application;

import kitchenpos.dao.OrderDao;
import kitchenpos.dao.OrderTableDao;
import kitchenpos.dao.TableGroupDao;
import kitchenpos.domain.Order;
import kitchenpos.domain.OrderTable;
import kitchenpos.domain.TableGroup;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Transactional
@SpringBootTest
class TableGroupServiceTest {

    @Autowired
    private TableGroupService tableGroupService;

    @Autowired
    private OrderTableDao orderTableDao;

    @Autowired
    private TableGroupDao tableGroupDao;

    @Autowired
    private OrderDao orderDao;

    private OrderTable testTable1;
    private OrderTable testTable2;

    @BeforeEach
    void setup() {
        final OrderTable orderTable1 = new OrderTable();
        orderTable1.setNumberOfGuests(0);
        orderTable1.setEmpty(true);
        testTable1 = orderTableDao.save(orderTable1);

        final OrderTable orderTable2 = new OrderTable();
        orderTable2.setNumberOfGuests(0);
        orderTable2.setEmpty(true);
        testTable2 = orderTableDao.save(orderTable2);
    }

    @Nested
    @DisplayName("테이블 그룹 생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("테이블 그룹 생성에 성공한다")
        void success() {
            // given
            final TableGroup tableGroupRequest = new TableGroup();
            tableGroupRequest.setOrderTables(List.of(testTable1, testTable2));

            // when
            final TableGroup response = tableGroupService.create(tableGroupRequest);

            // then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(response.getId()).isNotNull();
                final List<Long> tableIds = response.getOrderTables()
                        .stream()
                        .map(OrderTable::getId)
                        .collect(Collectors.toList());
                softly.assertThat(tableIds).containsExactlyInAnyOrderElementsOf(List.of(testTable1.getId(), testTable2.getId()));
            });
        }

        @Test
        @DisplayName("생성되지 않은 태이블로 그룹생성시 예외가 발생한다.")
        void throwExceptionWhenTableIsNotCreated() {
            // given
            final TableGroup tableGroupRequest = new TableGroup();
            final OrderTable notCreatedTable = new OrderTable();
            notCreatedTable.setId(-1L);
            tableGroupRequest.setOrderTables(List.of(testTable1, notCreatedTable));

            // when
            // then
            Assertions.assertThatThrownBy(() -> tableGroupService.create(tableGroupRequest))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("비어있지 않는 테이블로는 그룹을 생성시 예외가 발생한다.")
        void throwExceptionWithEmptyTable() {
            // given
            final TableGroup tableGroupRequest = new TableGroup();
            final OrderTable notEmptyTable = new OrderTable();
            notEmptyTable.setEmpty(false);
            final OrderTable savedNotEmptyTable = orderTableDao.save(notEmptyTable);
            tableGroupRequest.setOrderTables(List.of(testTable1, savedNotEmptyTable));

            // when
            // then
            Assertions.assertThatThrownBy(() -> tableGroupService.create(tableGroupRequest))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("이미 그룹지정된 태이블로는 생성시 예외가 발생한다.")
        void throwExceptionWithAlreadyGroupedTable() {
            // given
            final TableGroup otherTableGroup = new TableGroup();
            otherTableGroup.setCreatedDate(LocalDateTime.now());
            final TableGroup savedOtherTableGroup = tableGroupDao.save(otherTableGroup);

            final OrderTable groupedTable = new OrderTable();
            groupedTable.setEmpty(true);
            groupedTable.setTableGroupId(savedOtherTableGroup.getId());
            final OrderTable savedGroupedTable = orderTableDao.save(groupedTable);

            final TableGroup tableGroupRequest = new TableGroup();
            tableGroupRequest.setOrderTables(List.of(testTable1, savedGroupedTable));

            // when
            // then
            Assertions.assertThatThrownBy(() -> tableGroupService.create(tableGroupRequest))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("테이블 그룹을 해체할 수 있다.")
    class UnGroupTest {

        private TableGroup testTableGroup;

        @BeforeEach
        void setup() {
            final TableGroup tableGroup = new TableGroup();
            tableGroup.setOrderTables(List.of(testTable1, testTable2));
            tableGroup.setCreatedDate(LocalDateTime.now());
            testTableGroup = tableGroupDao.save(tableGroup);

            testTable1.setId(null);
            testTable1.setTableGroupId(testTableGroup.getId());
            testTable1 = orderTableDao.save(testTable1);

            testTable2.setId(null);
            testTable2.setTableGroupId(testTableGroup.getId());
            testTable2 = orderTableDao.save(testTable2);
        }

        @Test
        @DisplayName("그룹 해체에 성공한다.")
        void success() {
            // given

            // when
            tableGroupService.ungroup(testTableGroup.getId());

            // then
            final OrderTable actualTable1 = orderTableDao.findById(testTable1.getId()).get();
            final OrderTable actualTable2 = orderTableDao.findById(testTable2.getId()).get();
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(actualTable1.getTableGroupId()).isNull();
                softly.assertThat(actualTable2.getTableGroupId()).isNull();
            });
        }

        @ParameterizedTest(name = "테이블 주문 상태 : {0}")
        @ValueSource(strings = {"COOKING", "MEAL"})
        @DisplayName("완료상태가 아닌 주문이 있는경우 그룹해제시 예외가 발생한다.")
        void throwExceptionWithUncompletedOrder(final String status) {
            // given
            final Order order = new Order();
            order.setOrderStatus(status);
            order.setOrderedTime(LocalDateTime.now());
            order.setOrderTableId(testTable1.getId());
            orderDao.save(order);

            // when
            // then
            Assertions.assertThatThrownBy(() -> tableGroupService.ungroup(testTableGroup.getId()))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
