package freelance.home.comtrading.domain.item;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

public @Data class TableRecord {

    private Long total;
    private Long totalNotFiltered;

    private List<Item> rows = new ArrayList<>();
}
