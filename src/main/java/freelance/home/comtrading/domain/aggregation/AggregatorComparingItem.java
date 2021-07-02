package freelance.home.comtrading.domain.aggregation;

import freelance.home.comtrading.domain.item.Item;
import java.math.BigDecimal;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Entity
@Table(name = "aggregator_comparing_item")
@NoArgsConstructor
@Accessors(chain = true)
public @Data class AggregatorComparingItem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne//(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    private Aggregator aggregator;

    private AggregationStatus aggregationStatus = AggregationStatus.NOT_STARTED;

    private BigDecimal price;
    private String link;
    private Boolean available;

}
