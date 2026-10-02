package com.sparta.coffee.domain.menu.entity;

import com.sparta.coffee.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "menus")
public class Menu extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private int price;

    private int stock;

    @Enumerated(EnumType.STRING)
    private MenuStatus status;

    @Builder
    public Menu(String name, int price, int stock, MenuStatus status) {
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.status = status;
    }
}
