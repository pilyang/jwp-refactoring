package kitchenpos.application.dto;

public class TableCreateRequest {

    private final Integer numberOfGuest;
    private final Boolean empty;

    public TableCreateRequest(final Integer numberOfGuest, final Boolean empty) {
        this.numberOfGuest = numberOfGuest;
        this.empty = empty;
    }

    public Integer getNumberOfGuest() {
        return numberOfGuest;
    }

    public Boolean getEmpty() {
        return empty;
    }
}
