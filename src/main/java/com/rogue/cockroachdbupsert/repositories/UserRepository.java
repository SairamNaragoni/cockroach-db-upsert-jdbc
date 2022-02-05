package com.rogue.cockroachdbupsert.repositories;

import com.rogue.cockroachdbupsert.models.User;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends ExtendedJdbcRepository<User> {

    int save(User entity);

    int[][] saveAll(List<User> entities);

}
