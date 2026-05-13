package tests.config;

import org.aeonbits.owner.Config;

@Config.LoadPolicy(Config.LoadType.MERGE)
@Config.Sources({"classpath:${env}.properties", "classpath:local.properties"})
public interface WebConfig extends Config {

    @Key("browser")
    @DefaultValue("chrome")
    String browser();

    @Key("browserVersion")
    @DefaultValue("128.0")
    String browserVersion();

    @Key("browserSize")
    @DefaultValue("1920x1080")
    String browserSize();

    @Key("uiBaseUri")
    @DefaultValue("https://book-club.qa.guru")
    String uiBaseUri();

    @Key("remoteUrl")
    String remoteUrl();

    @Key("headless")
    @DefaultValue("false")
    Boolean headless();
}
