package com.trading.rdbs.order;

import com.trading.rdbs.account.AccountRepository;
import com.trading.rdbs.account.domain.Account;
import com.trading.rdbs.common.ResourceNotFoundException;
import com.trading.rdbs.order.domain.Order;
import com.trading.rdbs.order.dto.OrderRequest;
import com.trading.rdbs.order.dto.OrderResponse;
import com.trading.rdbs.symbol.SymbolRepository;
import com.trading.rdbs.symbol.domain.Symbol;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 【職責】委託單業務；建立時以 FK 連結 Account 與 Symbol（3NF + 1→N→1）。
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final AccountRepository accountRepository;
    private final SymbolRepository symbolRepository;

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account not found with id: " + request.getAccountId()));
        Symbol symbol = symbolRepository.findById(request.getSymbolId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Symbol not found with id: " + request.getSymbolId()));

        Order order = Order.builder()
                .side(request.getSide())
                .quantity(request.getQuantity())
                .unitPrice(request.getUnitPrice())
                .symbol(symbol)
                .build();
        account.addOrder(order);

        Order saved = orderRepository.save(order);
        return OrderResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long id) {
        Order order = orderRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        return OrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> listAll() {
        return orderRepository.findAllWithRelations().stream()
                .map(OrderResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> listByAccount(Long accountId) {
        if (!accountRepository.existsById(accountId)) {
            throw new ResourceNotFoundException("Account not found with id: " + accountId);
        }
        return orderRepository.findByAccountIdOrderByCreatedAtDesc(accountId).stream()
                .map(OrderResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> listBySymbol(Long symbolId) {
        if (!symbolRepository.existsById(symbolId)) {
            throw new ResourceNotFoundException("Symbol not found with id: " + symbolId);
        }
        return orderRepository.findBySymbolIdOrderByCreatedAtDesc(symbolId).stream()
                .map(OrderResponse::from)
                .toList();
    }
}
