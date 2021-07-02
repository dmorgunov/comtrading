package freelance.home.comtrading.repository;

import freelance.home.comtrading.domain.aggregation.AggregatorComparingItem;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RepositoryRestResource
public interface AggregatorComparingItemRepository extends PagingAndSortingRepository<AggregatorComparingItem, Long> {

    @Override
    <S extends AggregatorComparingItem> Iterable<S> saveAll(Iterable<S> iterable);
}