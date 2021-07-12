package freelance.home.comtrading.config;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class SeleniumWebDriverConfig {

    @Bean
    @Profile("local")
    public WebDriver localDriver() {
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

    @Bean
    @Profile("!local")
    public WebDriver notLocalDriver() throws MalformedURLException {
        ChromeOptions options = new ChromeOptions().setHeadless(true);
        options.addArguments("--disable-dev-shm-usage");
        DesiredCapabilities cap = DesiredCapabilities.chrome();
        cap.setCapability(ChromeOptions.CAPABILITY, options);
        LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
        cap.setJavascriptEnabled(true);
        cap.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);
        cap.setCapability("goog:loggingPrefs", logPrefs);
        String remote_url_chrome = "http://localhost:4444/wd/hub";
        WebDriver webDriver = new RemoteWebDriver(new URL(remote_url_chrome), cap);
        webDriver.manage().timeouts().implicitlyWait(3000, TimeUnit.MILLISECONDS);
        Dimension dimension = new Dimension(1920, 1180);
        webDriver.manage().window().fullscreen();
        webDriver.manage().window().setSize(dimension);
        return webDriver;
    }

}
