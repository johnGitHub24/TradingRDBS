package com.trading.rdbs.order;

import com.trading.rdbs.account.AccountRepository;
import com.trading.rdbs.account.domain.Account;
import com.trading.rdbs.common.ResourceNotFoundException;
import com.trading.rdbs.order.domain.Order;
import com.trading.rdbs.order.domain.OrderSide;
import com.trading.rdbs.order.dto.OrderRequest;
import com.trading.rdbs.order.dto.OrderResponse;
import com.trading.rdbs.support.RdbsTestFixtures;
import com.trading.rdbs.symbol.SymbolRepository;
import com.trading.rdbs.symbol.domain.Symbol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

/**
 * 【職責】OrderService 單元層；Case ID RDBS-001～006 與整合層成對。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Unit Tests (RDBS-001～006)")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private SymbolRepository symbolRepository;

    @InjectMocks
    private OrderService orderService;

    private Account account;
    private Symbol symbol;

    @BeforeEach
    void setUp() {
        account = Account.builder().id(1L).accountNo("ACC-001").ownerName("Alice").build();
        symbol = Symbol.builder().id(10L).ticker("2330").companyName("TSMC").exchangeCode("TWSE").build();
    }

    @Nested
    @DisplayName("createOrder()")
    class CreateOrder {

        @Test
        @DisplayName("RDBS-003 create order: links account (1) and symbol (1) via FK")
        void createOrder_validRequest_returnsOrderResponse() {
            OrderRequest request = RdbsTestFixtures.loadDto("order", "RDBS-003-SUCCESS", OrderRequest.class);

            given(accountRepository.findById(1L)).willReturn(Optional.of(account));
            given(symbolRepository.findById(10L)).willReturn(Optional.of(symbol));
            given(orderRepository.save(any(Order.class))).willAnswer(inv -> {
                Order o = inv.getArgument(0);
                o.setId(100L);
                o.setCreatedAt(LocalDateTime.now());
                return o;
            });

            OrderResponse result = orderService.createOrder(request);

            assertThat(result.id()).isEqualTo(100L);
            assertThat(result.accountId()).isEqualTo(1L);
            assertThat(result.symbolId()).isEqualTo(10L);
            assertThat(result.ticker()).isEqualTo("2330");
            then(orderRepository).should().save(any(Order.class));
        }

        @Test
        @DisplayName("RDBS-006 404: account not found")
        void createOrder_missingAccount_throwsNotFound() {
            OrderRequest request = RdbsTestFixtures.loadDto("order", "RDBS-006-NOT-FOUND-ACCOUNT", OrderRequest.class);

            given(accountRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.createOrder(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Account not found");
        }
    }

    @Nested
    @DisplayName("listBySymbol()")
    class ListBySymbol {

        @Test
        @DisplayName("RDBS-005 list by symbol: many orders share one symbol (N→1)")
        void listBySymbol_existingSymbol_returnsOrders() {
            Order order = Order.builder()
                    .id(1L).account(account).symbol(symbol)
                    .side(OrderSide.BUY).quantity(100)
                    .unitPrice(new BigDecimal("580")).createdAt(LocalDateTime.now())
                    .build();

            given(symbolRepository.existsById(10L)).willReturn(true);
            given(orderRepository.findBySymbolIdOrderByCreatedAtDesc(10L)).willReturn(List.of(order));

            List<OrderResponse> results = orderService.listBySymbol(10L);

            assertThat(results).hasSize(1);
            assertThat(results.get(0).ticker()).isEqualTo("2330");
        }

        @Test
        @DisplayName("RDBS-006 404: symbol not found")
        void listBySymbol_missingSymbol_throwsNotFound() {
            given(symbolRepository.existsById(99L)).willReturn(false);

            assertThatThrownBy(() -> orderService.listBySymbol(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getOrder()")
    class GetOrder {

        @Test
        @DisplayName("RDBS-006 404: order not found")
        void getOrder_missing_throwsNotFound() {
            given(orderRepository.findByIdWithRelations(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.getOrder(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
