package com.trading.rdbs.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trading.rdbs.account.dto.AccountRequest;
import com.trading.rdbs.order.domain.OrderSide;
import com.trading.rdbs.order.dto.OrderRequest;
import com.trading.rdbs.symbol.dto.SymbolRequest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 【職責】REST 整合層；Case ID RDBS-001～006 與 OrderServiceTest 成對。
 */
@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestPropertySource(properties = "startup.info.enabled=false")
@DisplayName("TradingRDBS Integration Tests (RDBS-001～006)")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TradingRdbsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static Long seededAccountId;
    private static Long seededSymbolId;

    @Test
    @Order(1)
    @DisplayName("RDBS-001 create account: POST /api/v1/accounts")
    void createAccount_returns201() throws Exception {
        AccountRequest request = new AccountRequest();
        request.setAccountNo("ACC-TEST-001");
        request.setOwnerName("Test User");

        MvcResult result = mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountNo").value("ACC-TEST-001"))
                .andExpect(jsonPath("$.ownerName").value("Test User"))
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();

        seededAccountId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    @Test
    @Order(2)
    @DisplayName("RDBS-002 create symbol: POST /api/v1/symbols")
    void createSymbol_returns201() throws Exception {
        SymbolRequest request = new SymbolRequest();
        request.setTicker("TEST");
        request.setCompanyName("Test Corp");
        request.setExchangeCode("TWSE");

        MvcResult result = mockMvc.perform(post("/api/v1/symbols")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ticker").value("TEST"))
                .andReturn();

        seededSymbolId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    @Test
    @Order(3)
    @DisplayName("RDBS-003 create order: 1 account → N orders → 1 symbol")
    void createOrder_linksAccountAndSymbol() throws Exception {
        OrderRequest request = new OrderRequest();
        request.setAccountId(seededAccountId);
        request.setSymbolId(seededSymbolId);
        request.setSide(OrderSide.BUY);
        request.setQuantity(10);
        request.setUnitPrice(new BigDecimal("100.0000"));

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountId").value(seededAccountId))
                .andExpect(jsonPath("$.symbolId").value(seededSymbolId))
                .andExpect(jsonPath("$.ticker").value("TEST"));
    }

    @Test
    @Order(4)
    @DisplayName("RDBS-004 get account with orders: GET /api/v1/accounts/{id}")
    void getAccount_includesOrderList() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/{id}", seededAccountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNo").value("ACC-TEST-001"))
                .andExpect(jsonPath("$.orders").isArray())
                .andExpect(jsonPath("$.orders.length()").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.orders[0].ticker").value("TEST"));
    }

    @Test
    @Order(5)
    @DisplayName("RDBS-005 list orders by symbol: GET /api/v1/orders?symbolId=")
    void listOrdersBySymbol_returnsMatchingOrders() throws Exception {
        mockMvc.perform(get("/api/v1/orders").param("symbolId", seededSymbolId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].symbolId").value(seededSymbolId))
                .andExpect(jsonPath("$[0].ticker").value("TEST"));
    }

    @Test
    @Order(6)
    @DisplayName("RDBS-006 404: GET /api/v1/accounts/999999")
    void getAccount_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/{id}", 999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @Order(7)
    @DisplayName("DataSeeder: seeded accounts and symbols exist")
    void seededData_present() throws Exception {
        mockMvc.perform(get("/api/v1/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))));

        mockMvc.perform(get("/api/v1/symbols"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))));
    }
}
