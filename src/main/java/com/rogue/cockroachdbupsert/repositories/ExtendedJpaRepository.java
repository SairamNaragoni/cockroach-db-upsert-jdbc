package com.rogue.cockroachdbupsert.repositories;

import com.rogue.cockroachdbupsert.models.User;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

@NoRepositoryBean
public interface ExtendedJpaRepository<T>{

    T upsert(T entity);

    List<T> upsertAll(List<T> entities);

    List<User> upsertAllParallel(List<User> entities);
}
