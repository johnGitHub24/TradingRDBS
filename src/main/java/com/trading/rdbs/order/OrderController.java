package com.trading.rdbs.order;

import com.trading.rdbs.order.dto.OrderRequest;
import com.trading.rdbs.order.dto.OrderResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request) {
        OrderResponse created = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public OrderResponse getOrder(@PathVariable Long id) {
        return orderService.getOrder(id);
    }

    @GetMapping
    public List<OrderResponse> listOrders(
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) Long symbolId) {
        if (accountId != null) {
            return orderService.listByAccount(accountId);
        }
        if (symbolId != null) {
            return orderService.listBySymbol(symbolId);
        }
        return orderService.listAll();
    }
}
