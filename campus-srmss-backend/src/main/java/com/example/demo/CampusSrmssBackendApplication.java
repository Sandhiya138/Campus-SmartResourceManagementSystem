package com.example.demo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CampusSrmssBackendApplication {

	@Bean
CommandLineRunner seedAdmin(UserRepository users, PasswordEncoder encoder) {
    return args -> {
        if (users.findByUsername("admin").isEmpty()) {
            User a = new User();
            a.username = "admin";
            a.password = encoder.encode("admin123");
            a.role = "ADMIN";
            a.status = "APPROVED";
            users.save(a);
        }
    };
}
	public static void main(String[] args) {
		SpringApplication.run(CampusSrmssBackendApplication.class, args);
	}

}
