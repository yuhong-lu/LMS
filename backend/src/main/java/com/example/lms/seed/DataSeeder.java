package com.example.lms.seed;

import com.example.lms.entity.User;
import com.example.lms.entity.UserRole;
import com.example.lms.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final String adminUsername;
  private final String adminPassword;

  public DataSeeder(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      @Value("${app.admin.username}") String adminUsername,
      @Value("${app.admin.password}") String adminPassword) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.adminUsername = adminUsername;
    this.adminPassword = adminPassword;
  }

  @Override
  public void run(String... args) {
    ensureAdminUser();
  }

  private void ensureAdminUser() {
    User user =
        userRepository
            .findByUsername(adminUsername)
            .orElseGet(
                () -> {
                  User created = new User();
                  created.setUsername(adminUsername);
                  created.setEmail(adminUsername + "@example.com");
                  created.setPassword(passwordEncoder.encode(adminPassword));
                  return created;
                });
    // 强制保证指定用户名的账号拥有管理员角色并处于可用状态
    user.setRole(UserRole.ROLE_ADMIN);
    user.setEnabled(true);
    if (user.getEmail() == null || user.getEmail().isBlank()) {
      user.setEmail(adminUsername + "@example.com");
    }
    // 如果是新建或想重置密码，可在配置中修改 adminPassword；已有账号时保留原密码
    if (user.getId() == null) {
      user.setPassword(passwordEncoder.encode(adminPassword));
    }
    userRepository.save(user);
  }
}
