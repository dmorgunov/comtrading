package freelance.home.comtrading.manager;

import freelance.home.comtrading.domain.proxy.Proxy;
import freelance.home.comtrading.service.ConfigService;
import freelance.home.comtrading.service.ProxyService;
import org.jsoup.Jsoup;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class ProxyManager {
    private final Logger log = Logger.getLogger(ProxyManager.class.getName());
    private static final List<Proxy> CACHE_PROXIES = new ArrayList<>();

    private final ProxyService proxyService;
    private final ConfigService configService;

    public ProxyManager(ProxyService proxyService, ConfigService configService) {
        this.proxyService = proxyService;
        this.configService = configService;
    }

//    @Scheduled(fixedDelay = 60_000 * 25)
    public void loadNewProxy() throws Exception {
        log.info("----------------- ПРОКСИЧЕКЕР [START] -----------------");

        List<String> apiList = configService.getApiList();
        if (apiList.size() == 0) throw new Exception("Лист прокси API, пустой. Отмена запуска!");

        log.info("Получаем лист сырых прокси");
        List<String> allProxy = new ArrayList<>();
        for (String api : apiList) {
            try {
                String response = Jsoup.connect(api)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:64.0) Gecko/20100101 Firefox/64.0")
                        .timeout(15_000)
                        .ignoreContentType(true)
                        .execute().body();

                validProxies(response);
                allProxy.add(response);
            } catch (Exception e) {
                log.warning("Не удалось считать прокси: " + api);
                log.warning("-------------------------------------------------");
            }
        }
        if (allProxy.size() == 0) throw new Exception("Было получено 0 прокси листов");

        log.info("Начинаем обработку полученных прокси");
        CACHE_PROXIES.clear();

        List<String> proxies = new ArrayList<>(Arrays.asList(String.join("\n", allProxy).split("\\r?\\n")));
        CACHE_PROXIES.addAll(proxies.stream().map(Proxy::new).distinct().collect(Collectors.toList()));
        proxyService.updateAllProxy(CACHE_PROXIES);

        log.info("Обновляем время timeout и restTime для прокси");
        Proxy.TIME_OUT = configService.getProxyTimeOut() * 1_000;
        Proxy.REST_TIME = configService.getProxyRestTime() * 1_000;

        log.info("----------------- ПРОКСИЧЕКЕР [END] -----------------");
//        System.exit(0);
    }

    private void validProxies(String response) throws Exception {
        List<String> proxies = Arrays.asList(response.split("\\r?\\n"));
        proxies = proxies.stream().filter(this::isProxyValid).collect(Collectors.toList());

        if (proxies.size() <= 250 || response.contains("html"))
            throw new Exception("Proxies are not valid");
    }

    private boolean isProxyValid(String proxy) {
        return Pattern.compile("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5]):[0-9]+$").matcher(proxy).find();
    }
    // -------------------------------------------------

    public Proxy getBestProxy() {
        synchronized(CACHE_PROXIES) {
            if (CACHE_PROXIES.isEmpty()) CACHE_PROXIES.addAll(proxyService.getAllProxies());

            Proxy proxy = CACHE_PROXIES.stream()
                    .filter(c -> !c.isInUse())
                    .max(Comparator.comparingInt(Proxy::getScore))
                    .orElse(null);

            if (proxy != null) {
                proxy.setLastUsed(new Date().getTime());
                proxy.setInUse(true);
            }

            return proxy;
        }
    }

    public int getProxySize() {
        if (CACHE_PROXIES.isEmpty()) CACHE_PROXIES.addAll(proxyService.getAllProxies());
        return (int) (CACHE_PROXIES.size() * 0.9);
    }

    public void releaseAllProxy() {
        synchronized(CACHE_PROXIES) {
            CACHE_PROXIES.forEach(p->p.setInUse(false));
        }
    }
}