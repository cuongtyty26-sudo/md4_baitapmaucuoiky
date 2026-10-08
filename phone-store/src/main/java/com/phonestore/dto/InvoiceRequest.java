package com.phonestore.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

public record InvoiceRequest(
        @NotNull(message = "Vui lòng chọn khách hàng") Long customerId,
        @NotEmpty(message = "Đơn hàng phải có ít nhất 1 sản phẩm") List<@Valid Item> items) {

    public record Item(
            @NotNull(message = "Thiếu sản phẩm") Long productId,
            @NotNull(message = "Thiếu số lượng") @Min(value = 1, message = "Số lượng phải >= 1") Integer quantity) {
    }
}
