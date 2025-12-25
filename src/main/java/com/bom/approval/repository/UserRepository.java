package com.bom.approval.repository;

import com.bom.approval.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;
import java.util.List;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByUserName(String userName);
    List<User> findByRole(String role);
}
