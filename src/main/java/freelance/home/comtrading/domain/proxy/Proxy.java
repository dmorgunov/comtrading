package freelance.home.comtrading.domain.proxy;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "proxy")
@NoArgsConstructor
public @Data class Proxy {
    public static int TIME_OUT = 30 * 1000;
    public static int REST_TIME = 35 * 1000;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String proxy;
    private long lastUsed = 0L;
    private int failCount = 0;
    private int successCount = 0;

    private boolean inUse = false;
    // -----------------------------

    public Proxy(String proxy) {
        this.proxy = proxy;
    }

    public int getScore() {
        long timeSinceLastUse = new Date().getTime() - lastUsed;
        int score = successCount - failCount;
        return (timeSinceLastUse > REST_TIME ? 1000 : (timeSinceLastUse < (REST_TIME / 2) ? -500 : 0)) + score;
    }

    public RequestConfig getReadyProxy() {
        return RequestConfig.custom()
                .setCookieSpec("standard")
                .setProxy(new HttpHost(this.getHostName(), this.getPort(), "http"))
                .setConnectionRequestTimeout(TIME_OUT)
                .setSocketTimeout(TIME_OUT)
                .setConnectTimeout(TIME_OUT)
                .build();
    }

    public String getHostName() {
        return proxy.split(":")[0];
    }

    public Integer getPort() {
        return Integer.parseInt(proxy.split(":")[1]);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Proxy proxy1 = (Proxy) o;
        return Objects.equals(proxy, proxy1.proxy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(proxy);
    }
}