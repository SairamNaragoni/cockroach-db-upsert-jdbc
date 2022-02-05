package com.rogue.cockroachdbupsert.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.util.UUID;

@Table(value = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User  {
    @Id
    @Column(value = "id")
    private UUID id;

    @Column(value = "city")
    private String city;

    @Column(value = "name")
    private String name;

    @Column(value = "address")
    private String address;

    @Column(value = "credit_card")
    private String credit_card;
}
