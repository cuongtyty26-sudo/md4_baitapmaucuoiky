package com.phonestore.controller;

import com.phonestore.dto.InvoiceRequest;
import com.phonestore.dto.StatusRequest;
import com.phonestore.entity.Invoice;
import com.phonestore.service.InvoiceService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {
    private final InvoiceService service;

    public InvoiceController(InvoiceService service) { this.service = service; }

    @GetMapping
    public Page<Invoice> list(@RequestParam(required = false) String keyword,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size) {
        return service.search(keyword, page, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Invoice create(@Valid @RequestBody InvoiceRequest req) { return service.create(req); }

    @PatchMapping("/{id}/status")
    public Invoice updateStatus(@PathVariable Long id, @Valid @RequestBody StatusRequest req) {
        return service.updateStatus(id, req.status());
    }
}
