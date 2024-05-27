package luckyseven.dart.global.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (isLocalProfileActive(environment)) {
            Dotenv dotenv = Dotenv.load();
            Map<String, Object> envVars = new HashMap<>();
            dotenv.entries().forEach(entry -> envVars.put(entry.getKey(), entry.getValue()));

            environment.getPropertySources().addLast(new MapPropertySource("dotenv", envVars));
        }
    }

    private boolean isLocalProfileActive(ConfigurableEnvironment environment) {
        for (String profile : environment.getActiveProfiles()) {
            if (profile.equalsIgnoreCase("local")) {
                return true;
            }
        }
        return false;
    }
}
