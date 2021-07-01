package freelance.home.comtrading.service;

import freelance.home.comtrading.domain.proxy.Config;
import freelance.home.comtrading.repository.ConfigRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class ConfigService {

    private final ConfigRepository configRepository;

    public ConfigService(ConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    public List<String> getApiList() {
        return getConfig().getApiList();
    }
    // ------------------------------------------------------
    public Integer getProxyTimeOut() {
        return getConfig().getProxyTimeOut();
    }
    public Integer getProxyRestTime() {
        return getConfig().getProxyRestTime();
    }

    public Integer getThreadSizeMin() {
        return getConfig().getThreadSizeMin();
    }
    public Integer getThreadSizeMax() {
        return getConfig().getThreadSizeMax();
    }

    public Integer getLoadCycleTimeOut() {
        return getConfig().getLoadCycleTimeOut();
    }

    /* --------- Support methods --------- */
    private Config createDefaultConfig() {
        configRepository.deleteAll();

        Config config = new Config();

        config.setProxyTimeOut(30);
        config.setProxyRestTime(35);

        config.setThreadSizeMin(256);
        config.setThreadSizeMax(512);

        config.setLoadCycleTimeOut(35);

        config.setApiList(Arrays.asList(
                "https://api.best-proxies.ru/proxylist.txt?key=d08080e13e2d80c06c648e27cc8d907b&type=http,https&limit=0",
                "https://api.good-proxies.ru/get.php?type[http]=on&access[%27supportsHttps%27]=on&count=&ping=50000&time=600&works=100&key=bc4faa5b94e480d54239009ed8d79d2e"
        ));

        configRepository.save(config);
        return config;
    }

    private Config getConfig() {
        return configRepository.existsById(0L) ? configRepository.getOne(0L) : createDefaultConfig();
    }
}
