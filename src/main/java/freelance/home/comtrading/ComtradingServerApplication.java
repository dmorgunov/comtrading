package freelance.home.comtrading;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ComtradingServerApplication {
	public static void main(String[] args) {
		SpringApplication.run(ComtradingServerApplication.class, args);
	}
}