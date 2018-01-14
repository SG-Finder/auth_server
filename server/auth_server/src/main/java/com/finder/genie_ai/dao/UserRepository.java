package com.finder.genie_ai.dao;

import com.finder.genie_ai.model.user.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserModel, Integer> {

    Optional<UserModel> findByUserId(String userId);

}
