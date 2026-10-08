package com.phonestore.dto;

import com.phonestore.entity.InvoiceStatus;
import jakarta.validation.constraints.NotNull;

public record StatusRequest(@NotNull(message = "Thiếu trạng thái") InvoiceStatus status) {
}
