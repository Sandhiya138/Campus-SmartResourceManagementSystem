package com.example.demo.service;
import com.example.demo.entity.User;
import com.example.demo.exception.ApiException;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class AuthService {
    @Autowired UserRepository users;
    @Autowired PasswordEncoder encoder;
    @Autowired JwtUtil jwtUtil;
    @Autowired AuditService audit;

    public User register(Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        String role = body.get("role");
        if (username == null || username.isBlank() || password == null || password.isBlank())
            throw new ApiException(400, "Username and password are required");
        if (!"STUDENT".equals(role) && !"FACULTY".equals(role))
            throw new ApiException(400, "Role must be STUDENT or FACULTY");
        if (users.findByUsername(username).isPresent())
            throw new ApiException(409, "Username already exists");

        User u = new User();
        u.username = username;
        u.password = encoder.encode(password);   // hashed
        u.role = role;
        u.status = "PENDING";                    // admin must approve
        users.save(u);
        audit.log(u.id, "USER_REGISTERED");
        return u;
    }

    public Map<String, Object> login(Map<String, String> body) {
        User u = users.findByUsername(body.get("username"))
                .orElseThrow(() -> new ApiException(401, "Invalid username or password"));
        String pw = body.get("password") == null ? "" : body.get("password");
        if (!encoder.matches(pw, u.password))
            throw new ApiException(401, "Invalid username or password");
        if ("PENDING".equals(u.status)) throw new ApiException(403, "Your account is waiting for admin approval");
        if ("REJECTED".equals(u.status)) throw new ApiException(403, "Your account was rejected");

        audit.log(u.id, "USER_LOGIN");
        return Map.of("token", jwtUtil.generate(u.username, u.role), "username", u.username, "role", u.role);
    }
}
