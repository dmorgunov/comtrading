package freelance.home.comtrading.domain.item;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "item")
@NoArgsConstructor
public @Data class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long feedId;

    private String model;
    private String sku;

    @Column(columnDefinition = "TEXT")
    private String name;

    private Long categoryId;
    private String categoryName;

    private Double suplPrice;
    private Double price;
    // -----------------------------------

    @Column(columnDefinition = "TEXT")
    private String autoSearchUrl = null;
    // -----------------------------------

    private Status status = Status.ADDED;
    private ParseStatus parseStatus = ParseStatus.UNKNOWN;
}
