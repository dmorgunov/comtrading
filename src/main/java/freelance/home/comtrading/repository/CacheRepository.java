package freelance.home.comtrading.repository;


import freelance.home.comtrading.domain.request.RequestTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface CacheRepository extends JpaRepository<RequestTask, Long> {

    @Modifying
    @Query(value = "TRUNCATE request;", nativeQuery = true)
    void truncate();

    boolean existsByCacheId(Integer cacheId);
    RequestTask findFirstByCacheId(Integer cacheId);
}