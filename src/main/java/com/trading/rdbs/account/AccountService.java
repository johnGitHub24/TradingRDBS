package com.trading.rdbs.account;

import com.trading.rdbs.account.domain.Account;
import com.trading.rdbs.account.dto.AccountDetailResponse;
import com.trading.rdbs.account.dto.AccountRequest;
import com.trading.rdbs.account.dto.AccountResponse;
import com.trading.rdbs.common.ResourceNotFoundException;
import com.trading.rdbs.order.OrderRepository;
import com.trading.rdbs.order.dto.OrderSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 【職責】帳戶 CRUD；讀取時可帶出關聯訂單（1→N）。
 */
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public List<AccountResponse> listAccounts() {
        return accountRepository.findAll().stream()
                .map(AccountResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public AccountDetailResponse getAccount(Long id) {
        Account account = findAccountOrThrow(id);
        List<OrderSummaryResponse> orders = orderRepository.findByAccountIdOrderByCreatedAtDesc(id).stream()
                .map(OrderSummaryResponse::from)
                .toList();
        return AccountDetailResponse.from(account, orders);
    }

    @Transactional
    public AccountResponse createAccount(AccountRequest request) {
        Account account = Account.builder()
                .accountNo(request.getAccountNo())
                .ownerName(request.getOwnerName())
                .build();
        return AccountResponse.from(accountRepository.save(account));
    }

    private Account findAccountOrThrow(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + id));
    }
}
