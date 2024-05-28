package luckyseven.dart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class DartApplication {

	public static void main(String[] args) {
		SpringApplication.run(DartApplication.class, args);
	}

	@GetMapping("/health")
	public String healthCheck() {
		return "[✅ SUCCESS] Application is up and running!";
	}
}
