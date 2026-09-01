package com.trading.rdbs.order;

import com.trading.rdbs.order.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT o FROM Order o JOIN FETCH o.account JOIN FETCH o.symbol ORDER BY o.createdAt DESC")
    List<Order> findAllWithRelations();

    @Query("SELECT o FROM Order o JOIN FETCH o.account JOIN FETCH o.symbol "
            + "WHERE o.account.id = :accountId ORDER BY o.createdAt DESC")
    List<Order> findByAccountIdOrderByCreatedAtDesc(@Param("accountId") Long accountId);

    @Query("SELECT o FROM Order o JOIN FETCH o.account JOIN FETCH o.symbol "
            + "WHERE o.symbol.id = :symbolId ORDER BY o.createdAt DESC")
    List<Order> findBySymbolIdOrderByCreatedAtDesc(@Param("symbolId") Long symbolId);

    @Query("SELECT o FROM Order o JOIN FETCH o.account JOIN FETCH o.symbol WHERE o.id = :id")
    Optional<Order> findByIdWithRelations(@Param("id") Long id);
}
