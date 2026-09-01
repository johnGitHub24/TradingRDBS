package com.trading.rdbs.order.dto;

import com.trading.rdbs.order.domain.Order;
import com.trading.rdbs.order.domain.OrderSide;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OrderRequest {

    @NotNull
    private Long accountId;

    @NotNull
    private Long symbolId;

    @NotNull
    private OrderSide side;

    @NotNull
    @Min(1)
    private Integer quantity;

    @NotNull
    @DecimalMin(value = "0.0001")
    private BigDecimal unitPrice;
}
