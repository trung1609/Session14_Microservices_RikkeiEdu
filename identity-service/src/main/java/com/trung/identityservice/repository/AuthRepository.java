package com.trung.identityservice.repository;

import com.trung.identityservice.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthRepository extends JpaRepository<Users, String> {
    Optional<Users> findByUsername(String username);
}
