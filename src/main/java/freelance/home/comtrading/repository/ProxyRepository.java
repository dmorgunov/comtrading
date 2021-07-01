package freelance.home.comtrading.repository;

import freelance.home.comtrading.domain.proxy.Proxy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProxyRepository extends JpaRepository<Proxy, Long> {
}