package com.sprintly.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Setter
@Entity
@Table(name = "columns")
@SQLDelete(sql = "UPDATE columns SET is_deleted = true, updated_at = NOW() WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class BoardColumn extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "position", nullable = false)
    private int position;
}
