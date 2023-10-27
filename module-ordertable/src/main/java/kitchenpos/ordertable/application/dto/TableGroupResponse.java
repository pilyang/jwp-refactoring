package kitchenpos.ordertable.application.dto;

import kitchenpos.ordertable.domain.OrderTable;
import kitchenpos.ordertable.domain.TableGroup;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class TableGroupResponse {

    private final Long id;
    private final LocalDateTime createdDate;
    private final List<TableResponse> tableResponses;

    private TableGroupResponse(final Long id, final LocalDateTime createdDate, final List<TableResponse> tableResponses) {
        this.id = id;
        this.createdDate = createdDate;
        this.tableResponses = tableResponses;
    }

    public static TableGroupResponse from(final TableGroup tableGroup, final List<OrderTable> orderTables) {
        return new TableGroupResponse(
                tableGroup.getId(),
                tableGroup.getCreatedDate(),
                orderTables.stream()
                        .map(TableResponse::from)
                        .collect(Collectors.toUnmodifiableList())
        );
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public List<TableResponse> getTableResponses() {
        return tableResponses;
    }
}
