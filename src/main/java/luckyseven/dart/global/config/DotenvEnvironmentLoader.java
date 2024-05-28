package luckyseven.dart.global.config;

import io.github.cdimascio.dotenv.Dotenv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DotenvEnvironmentLoader {
    private static final Logger logger = LoggerFactory.getLogger(DotenvEnvironmentLoader.class);
    private static Dotenv dotenv;

    public static void loadEnv() {
        if (dotenv == null) {
            dotenv = Dotenv.configure().ignoreIfMalformed().ignoreIfMissing().load();
        }

        dotenv.entries().forEach(entry -> {
            System.setProperty(entry.getKey(), entry.getValue());
            logger.info("{}={}", entry.getKey(), entry.getValue());
        });
    }
}
