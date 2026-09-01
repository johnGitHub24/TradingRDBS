package com.trading.rdbs.account;

import com.trading.rdbs.account.domain.Account;
import com.trading.rdbs.account.dto.AccountDetailResponse;
import com.trading.rdbs.account.dto.AccountRequest;
import com.trading.rdbs.account.dto.AccountResponse;
import com.trading.rdbs.common.ResourceNotFoundException;
import com.trading.rdbs.order.OrderRepository;
import com.trading.rdbs.order.domain.Order;
import com.trading.rdbs.order.domain.OrderSide;
import com.trading.rdbs.symbol.domain.Symbol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountService Unit Tests (RDBS-001, RDBS-004, RDBS-006)")
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private AccountService accountService;

    private Account account;

    @BeforeEach
    void setUp() {
        account = Account.builder()
                .id(1L).accountNo("ACC-001").ownerName("Alice")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("RDBS-001 create account")
    void createAccount_savesAndReturns() {
        AccountRequest request = new AccountRequest();
        request.setAccountNo("ACC-NEW");
        request.setOwnerName("New User");

        given(accountRepository.save(org.mockito.ArgumentMatchers.any(Account.class)))
                .willAnswer(inv -> {
                    Account a = inv.getArgument(0);
                    a.setId(2L);
                    a.setCreatedAt(LocalDateTime.now());
                    return a;
                });

        AccountResponse response = accountService.createAccount(request);

        assertThat(response.accountNo()).isEqualTo("ACC-NEW");
        assertThat(response.id()).isEqualTo(2L);
    }

    @Test
    @DisplayName("RDBS-004 get account with orders (1→N)")
    void getAccount_withOrders_returnsDetail() {
        Symbol symbol = Symbol.builder().id(10L).ticker("2330").companyName("TSMC").exchangeCode("TWSE").build();
        Order order = Order.builder()
                .id(5L).account(account).symbol(symbol)
                .side(OrderSide.BUY).quantity(100)
                .unitPrice(new BigDecimal("580")).createdAt(LocalDateTime.now())
                .build();

        given(accountRepository.findById(1L)).willReturn(Optional.of(account));
        given(orderRepository.findByAccountIdOrderByCreatedAtDesc(1L)).willReturn(List.of(order));

        AccountDetailResponse detail = accountService.getAccount(1L);

        assertThat(detail.orders()).hasSize(1);
        assertThat(detail.orders().get(0).ticker()).isEqualTo("2330");
    }

    @Test
    @DisplayName("RDBS-006 404 account not found")
    void getAccount_notFound_throws() {
        given(accountRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getAccount(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
