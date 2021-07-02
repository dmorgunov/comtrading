package freelance.home.comtrading.repository;

import freelance.home.comtrading.domain.aggregation.Aggregator;
import freelance.home.comtrading.domain.item.Item;
import freelance.home.comtrading.domain.item.ParseStatus;
import freelance.home.comtrading.domain.item.Status;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RepositoryRestResource
public interface ItemRepository extends PagingAndSortingRepository<Item, Long> {

    @RestResource(exported = false)
    @Query("SELECT i FROM Item i WHERE i.status = :status")
    List<Item> getNotFilledItems(@Param("status") Status status, Pageable page);

    @RestResource(exported = false)
    @Query("SELECT i FROM Item i WHERE i.autoSearchUrl is not null")
    List<Item> getFilledItems(PageRequest page);

    @RestResource(exported = false)
    Item findByModel(String model);

    @Query("SELECT i FROM Item i WHERE i.feedId = :feedId")
    Item getItemByFeedId(@Param("feedId") Long feedId);

    @Query("SELECT i FROM Item i WHERE i.parseStatus = 0 and i.feedId in (:feedIds)")
    List<Item> getItemsByFeedIds(@Param("feedIds") Set<Long> feedIds);

    @Query("SELECT i FROM Item i WHERE i.parseStatus = :status")
    Page<Item> customFindAll(@Param("status") ParseStatus status, Pageable page);

    @Modifying
    @Query("UPDATE Item i SET i.autoSearchUrl = :url WHERE i.feedId = :feedId")
    void updateUrl(@Param("feedId") Long feedId, @Param("url") String url);

    @Modifying
    @Query("UPDATE Item i SET i.parseStatus = :status WHERE i.feedId = :feedId")
    void updateParseStatus(@Param("feedId") Long feedId, @Param("status") ParseStatus status);

    @Query("SELECT i FROM Item i WHERE i.parseStatus IN :status")
    Page<Item> getItemsFilteredByStatus(@Param("status") List<ParseStatus> status, Pageable page);

    @Query(value = "SELECT i FROM Item i WHERE i.id NOT IN " +
            "(SELECT aci.item.id FROM AggregatorComparingItem aci  WHERE aci.aggregator = :aggregator)")
    List<Item> getFirstNotProcessedByAggregator(@Param("aggregator") Aggregator aggregator, Pageable p);
}