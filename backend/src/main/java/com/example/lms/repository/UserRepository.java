package com.example.lms.repository;

import com.example.lms.entity.User;
import com.example.lms.entity.UserRole;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByUsername(String username);
  Optional<User> findByEmail(String email);
  long countByRole(UserRole role);
  List<User> findAllByRole(UserRole role);
  boolean existsByUsername(String username);
  boolean existsByEmail(String email);
}
