package freelance.home.comtrading.config;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SeleniumWebDriverConfig {

    @Bean
    public WebDriver driver() {
        io.github.bonigarcia.wdm.WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-dev-shm-usage");
        DesiredCapabilities cap = DesiredCapabilities.chrome();
        cap.setCapability(ChromeOptions.CAPABILITY, options);
        LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
        cap.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);
        cap.setCapability("goog:loggingPrefs", logPrefs);
        ChromeDriver webDriver = new ChromeDriver(cap);
        webDriver.manage().timeouts().pageLoadTimeout(30, TimeUnit.SECONDS);
        webDriver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
        webDriver.manage().window().maximize();
        return webDriver;
    }
}
