package freelance.home.comtrading.repository;

import freelance.home.comtrading.domain.proxy.Config;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfigRepository extends JpaRepository<Config, Long> {
}