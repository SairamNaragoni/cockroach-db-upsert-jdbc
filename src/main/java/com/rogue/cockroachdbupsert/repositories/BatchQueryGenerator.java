package com.rogue.cockroachdbupsert.repositories;

import com.rogue.cockroachdbupsert.exceptions.RecordUpsertException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BatchQueryGenerator<T>{

    private static final String COMMA = ",";
    private static final String OPEN_BRACKET = "(";
    private static final String CLOSE_BRACKET = ")";
    private static final int MAX_BIND_VARIABLE_LIMIT = 32767-1;

    private List<Field> getColumnFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while(clazz != Object.class){
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields.stream().filter(field -> field.isAnnotationPresent(Column.class)).collect(Collectors.toList());
    }

    private String populateTableColumns(List<Field> columnFields){
        StringBuilder queryBuilder = new StringBuilder();
        for(Field field : columnFields){
            String columnName=field.getAnnotation(Column.class).value();
            queryBuilder.append(columnName).append(COMMA);
        }
        queryBuilder.deleteCharAt(queryBuilder.lastIndexOf(COMMA));
        return queryBuilder.toString();
    }

    private String formInitialQuery(List<Field> columnFields, String tableName){
        return "UPSERT INTO " +
                tableName +
                OPEN_BRACKET +
                populateTableColumns(columnFields) +
                CLOSE_BRACKET +
                " VALUES ";
    }

    private String populateColumnParams(List<Field> columnFields, T entity) throws IllegalAccessException {
        StringBuilder valuesBuilder = new StringBuilder();
        valuesBuilder.append(OPEN_BRACKET);
        for(Field field : columnFields){
            field.setAccessible(true);
            final Object o = field.get(entity);
            if(o!=null)
                valuesBuilder.append('\'').append(o).append('\'');
            else
                valuesBuilder.append((Object)null);
            valuesBuilder.append(COMMA);
            field.setAccessible(false);
        }
        valuesBuilder.deleteCharAt(valuesBuilder.lastIndexOf(COMMA));
        valuesBuilder.append(CLOSE_BRACKET);
        return valuesBuilder.toString();
    }

    public String upsert(T entity) {
        Assert.notNull(entity, "Entity must not be null.");
        final List<Field> columnFields = getColumnFields(entity.getClass());
        Assert.notEmpty(columnFields,"Define the entity with proper JDBC annotations");
        final String query;
        try {
            query = formInitialQuery(columnFields, entity.getClass().getAnnotation(Table.class).value())
                    + populateColumnParams(columnFields,entity);
        } catch (IllegalAccessException e) {
            throw new RecordUpsertException("Error generating upsert query", e);
        }
        return query;
    }

    public List<String> upsertAll(List<T> entities) {
        Assert.notNull(entities, "Entity must not be null.");
        Assert.notEmpty(entities,"Entity must not be empty.");
        final T sampleEntity = entities.get(0);
        final List<Field> columnFields = getColumnFields(sampleEntity.getClass());
        Assert.notEmpty(columnFields,"Define the entity with proper JDBC annotations");

        final int bindVariableCount = columnFields.size()*entities.size();
        final int entitiesPerStatement = bindVariableCount > MAX_BIND_VARIABLE_LIMIT ? MAX_BIND_VARIABLE_LIMIT/columnFields.size() : entities.size();
        List<List<T>> partitionedEntityList = ListUtils.partition(entities, entitiesPerStatement);
        List<String> batchedQueries = new ArrayList<>();
        partitionedEntityList.forEach(partitionedEntities -> {
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append(formInitialQuery(columnFields, sampleEntity.getClass().getAnnotation(Table.class).value()));
            partitionedEntities.forEach(entity -> {
                try {
                    queryBuilder.append(populateColumnParams(columnFields, entity)).append(COMMA);
                } catch (IllegalAccessException e) {
                    throw new RecordUpsertException("Error generating batch upsertion query", e);
                }
            });
            queryBuilder.deleteCharAt(queryBuilder.lastIndexOf(COMMA));
            batchedQueries.add(queryBuilder.toString());
        });
        return batchedQueries;
    }
}
