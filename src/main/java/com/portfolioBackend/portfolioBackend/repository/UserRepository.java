package com.portfolioBackend.portfolioBackend.repository;

import com.portfolioBackend.portfolioBackend.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByEmail(String email);
    Optional<User> findByGoogleSubjectId(String googleSubjectId);
    boolean existsByEmail(String email);
}
