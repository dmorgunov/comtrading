package freelance.home.comtrading.service;

import freelance.home.comtrading.domain.proxy.Proxy;
import freelance.home.comtrading.repository.ProxyRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.logging.Logger;

@Service
public class ProxyService {
    private static final Logger log = Logger.getLogger(ProxyService.class.getName());

    private final ProxyRepository proxyRepository;

    public ProxyService(ProxyRepository proxyRepository) {
        this.proxyRepository = proxyRepository;
    }

    // -------------------- DB Calls --------------------
    public List<Proxy> getAllProxies() {
        return proxyRepository.findAll();
    }

    @Transactional
    public void updateAllProxy(List<Proxy> proxies) {
        proxyRepository.deleteAll();
        proxyRepository.saveAll(proxies);
    }
}