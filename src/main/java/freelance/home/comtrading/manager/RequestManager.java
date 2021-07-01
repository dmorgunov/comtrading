package freelance.home.comtrading.manager;

import freelance.home.comtrading.domain.proxy.Proxy;
import freelance.home.comtrading.domain.request.ReqTaskType;
import freelance.home.comtrading.domain.request.RequestTask;
import freelance.home.comtrading.service.CacheService;
import freelance.home.comtrading.service.ConfigService;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Component
public class RequestManager {
    private static final Logger log = Logger.getLogger(RequestManager.class.getName());
    private static final Boolean CACHE_MODE = true;

    private final ProxyManager proxyManager;
    private final CacheService cacheService;
    private final ConfigService config;

    public RequestManager(ProxyManager proxyManager, CacheService cacheService, ConfigService config) {
        this.proxyManager = proxyManager;
        this.cacheService = cacheService;
        this.config = config;
    }

    public List<RequestTask> execute(List<RequestTask> tasks) throws Exception {

        if (CACHE_MODE) cacheService.loadFromCache(tasks);
        HashSet<RequestTask> result = tasks.stream().filter(t -> !t.getHtml().isEmpty()).collect(Collectors.toCollection(HashSet::new));
        tasks.removeIf(t -> !t.getHtml().isEmpty());

        // ============================
        long start = new Date().getTime();
        int initResult = result.size();
        int maxConnects;
        maxConnects = Math.min(config.getThreadSizeMax(), Math.min(Math.max(tasks.size(), config.getThreadSizeMin()), proxyManager.getProxySize()));

        log.info("-------------------------------------------------");
        log.info("Инициализируем новую волну, осталось: " + tasks.size() + " тасков");

        // Регистрируем новый конектор менеджер
        PoolingHttpClientConnectionManager connPool = new PoolingHttpClientConnectionManager();
        connPool.setMaxTotal(maxConnects);
        connPool.setDefaultMaxPerRoute(maxConnects);

        CloseableHttpClient httpClient = HttpClients.custom().
                setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).
                setConnectionManager(connPool).
                setRedirectStrategy(new LaxRedirectStrategy()).
                setConnectionManagerShared(true).build();

        // Регестрируем пул потоков
        Thread[] threads = new Thread[maxConnects];

        int taskIndex = 0;
        for (int i = 0; i < maxConnects; i++) {
            if (taskIndex >= tasks.size()) taskIndex = 0;

            RequestTask task = tasks.get(taskIndex++);
            Proxy proxy = proxyManager.getBestProxy();
            if (proxy == null) break;

            HttpRequestBase httpRequest = new HttpGet(task.getUrl());
//            httpRequest.setHeader("user-agent", "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)");
//                httpRequest.setHeader("Accept-Language", "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)");
            httpRequest.setConfig(proxy.getReadyProxy());

            // Добавляем потоки
            threads[i] = new Thread(() -> {
                HttpEntity entity = null;

                try {
                    HttpClientContext context = HttpClientContext.create();
                    CloseableHttpResponse response = httpClient.execute(httpRequest, context);

                    System.out.println(response.getStatusLine());
                    if (response.getStatusLine().getStatusCode() == 200) {
                        entity = response.getEntity();
                        String body = EntityUtils.toString(entity, "UTF-8");

                        proxy.setSuccessCount(proxy.getSuccessCount() + 1);
                        task.setHtml(body.contains("https://hotline.ua/") ? body : "404");
                        result.add(task);
                    } else if (response.getStatusLine().getStatusCode() == 404) {
                        proxy.setSuccessCount(proxy.getSuccessCount() + 1);
                        task.setHtml("404");
                        result.add(task);
                    } else {
                        proxy.setFailCount(proxy.getFailCount() + 1);
                    }
                } catch (Exception ignore) {
                } finally {
                    try {
                        EntityUtils.consume(entity);
                    } catch (IOException ignore) { }
                }
            });

            threads[i].setDaemon(true);
            threads[i].start();
        }

        // Ждём завершения круга
        Thread.sleep(config.getLoadCycleTimeOut() * 1000);
        System.out.println("--------------------------------");
        connPool.close();
        httpClient.close();

        for (Thread thread : threads) {
            try { thread.join(); } catch (Exception ignore) { }
        }

        tasks.removeIf(t -> result.stream().anyMatch(r -> r.getTaskId().equals(t.getTaskId())));
        proxyManager.releaseAllProxy();

        // Выводим дебаг инфо
        int waveResult = result.size() - initResult;
        log.info("**********************************************");
        log.info("Получено результатов на волну: " + waveResult);
        log.info("Общее количество результата: " + result.size());
        log.info("TIME: " + ((new Date()).getTime() - start) + " ms");
        log.info("**********************************************");

        if (CACHE_MODE) cacheService.saveToCache(result);
//        result.removeIf(r -> r.getHtml().equals("404"));
        return new ArrayList<>(result);
    }
}