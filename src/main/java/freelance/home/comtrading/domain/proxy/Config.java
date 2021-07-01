package freelance.home.comtrading.domain.proxy;

import lombok.Data;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = "config")
public @Data class Config {
    @Id
    private final Long id = 0L;

    private Integer proxyTimeOut;
    private Integer proxyRestTime;

    private Integer threadSizeMin;
    private Integer threadSizeMax;

    private Integer loadCycleTimeOut;

    @ElementCollection(targetClass=String.class)
    private List<String> apiList;
}
