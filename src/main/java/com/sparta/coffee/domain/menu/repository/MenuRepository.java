package com.sparta.coffee.domain.menu.repository;

import com.sparta.coffee.domain.menu.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Menu m SET m.stock = m.stock - :quantity WHERE m.id = :id AND m.stock >= :quantity")
    int reduceStock(@Param("id") Long id, @Param("quantity") int quantity);
}
