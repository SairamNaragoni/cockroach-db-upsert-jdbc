package com.rogue.cockroachdbupsert.repositories;

import com.rogue.cockroachdbupsert.models.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class UserRepositoryImpl implements UserRepository{

    private final JdbcTemplate jdbcTemplate;
    private final BatchQueryGenerator<User> batchQueryGenerator;
    private final Environment environment;

    public UserRepositoryImpl(JdbcTemplate jdbcTemplate, BatchQueryGenerator<User> batchQueryGenerator,
                              Environment environment) {
        this.jdbcTemplate = jdbcTemplate;
        this.batchQueryGenerator = batchQueryGenerator;
        this.environment = environment;
    }

    @Override
    public User upsert(User entity) {
        jdbcTemplate.update(batchQueryGenerator.upsert(entity));
        return entity;
    }

    @Override
    public List<User> upsertAll(List<User> entities) {
        batchQueryGenerator.upsertAll(entities).forEach(jdbcTemplate::batchUpdate);
        return entities;
    }

    @Override
    public List<User> upsertAllParallel(List<User> entities) {
        batchQueryGenerator.upsertAll(entities).parallelStream().forEach(jdbcTemplate::batchUpdate);
        return entities;
    }

    @Override
    public int save(User entity) {
        String upsertQuery = environment.getProperty("upsertQuery");
        return jdbcTemplate.update(upsertQuery, entity.getId(), entity.getName(), entity.getCity());
    }

    @Override
    public int[][] saveAll(List<User> entities) {
        String upsertQuery = environment.getProperty("upsertQuery");
        final int batchSize = entities.size() > 250 ? 250 : Math.min(25, entities.size()) ;
        return jdbcTemplate.batchUpdate(upsertQuery, entities, batchSize, (ps, user) -> {
            ps.setString(1, user.getId().toString());
            ps.setString(2, user.getName());
            ps.setString(3, user.getCity());
        });
    }
}
