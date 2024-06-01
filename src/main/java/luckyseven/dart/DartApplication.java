package luckyseven.dart;


import io.github.cdimascio.dotenv.Dotenv;
import luckyseven.dart.global.config.DotenvEnvironmentLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
@EnableJpaAuditing
public class DartApplication {

	public static void main(String[] args) {
		configureEnvironment();

		SpringApplication.run(DartApplication.class, args);
	}

	private static void configureEnvironment() {
		Dotenv dotenv = Dotenv.configure().ignoreIfMalformed().ignoreIfMissing().load();
		String activeProfile = dotenv.get("SPRING_PROFILES_ACTIVE");

		if (activeProfile != null) {
			System.setProperty("spring.profiles.active", activeProfile);

			if (activeProfile.contains("local")) {
				DotenvEnvironmentLoader.loadEnv();
			}
		}
	}
}
