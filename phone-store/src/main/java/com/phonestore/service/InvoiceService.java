package com.phonestore.service;

import com.phonestore.dto.InvoiceRequest;
import com.phonestore.entity.*;
import com.phonestore.exception.ApiException;
import com.phonestore.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InvoiceService {
    private final InvoiceRepository invoiceRepo;
    private final CustomerRepository customerRepo;
    private final ProductRepository productRepo;

    public InvoiceService(InvoiceRepository invoiceRepo, CustomerRepository customerRepo, ProductRepository productRepo) {
        this.invoiceRepo = invoiceRepo;
        this.customerRepo = customerRepo;
        this.productRepo = productRepo;
    }

    public Page<Invoice> search(String kw, int page, int size) {
        return invoiceRepo.search(kw == null ? "" : kw.trim(), PageRequest.of(Math.max(page, 0), Math.max(size, 1)));
    }

    /** Tạo đơn hàng + trừ tồn kho trong cùng 1 transaction. */
    @Transactional
    public Invoice create(InvoiceRequest req) {
        Customer customer = customerRepo.findById(req.customerId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy khách hàng"));

        Invoice inv = new Invoice();
        inv.setCustomer(customer);
        inv.setCreatedAt(LocalDateTime.now());
        inv.setStatus(InvoiceStatus.PENDING);

        long total = 0;
        for (InvoiceRequest.Item it : req.items()) {
            Product p = productRepo.findById(it.productId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm #" + it.productId()));
            if (p.getStock() < it.quantity())
                throw new ApiException(HttpStatus.BAD_REQUEST,
                        "Sản phẩm \"" + p.getName() + "\" chỉ còn " + p.getStock() + " trong kho");
            p.setStock(p.getStock() - it.quantity());
            productRepo.save(p);

            InvoiceItem line = new InvoiceItem();
            line.setInvoice(inv);
            line.setProduct(p);
            line.setQuantity(it.quantity());
            line.setUnitPrice(p.getPrice());
            inv.getItems().add(line);
            total += p.getPrice() * it.quantity();
        }
        inv.setTotal(total);
        return invoiceRepo.save(inv);
    }

    /** Đổi trạng thái; chuyển sang CANCELLED thì hoàn lại tồn kho. */
    @Transactional
    public Invoice updateStatus(Long id, InvoiceStatus newStatus) {
        Invoice inv = invoiceRepo.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy hóa đơn"));
        if (inv.getStatus() == InvoiceStatus.CANCELLED)
            throw new ApiException(HttpStatus.BAD_REQUEST, "Hóa đơn đã huỷ, không thể đổi trạng thái");
        if (inv.getStatus() == newStatus) return inv;

        if (newStatus == InvoiceStatus.CANCELLED) {
            for (InvoiceItem line : inv.getItems()) {
                Product p = line.getProduct();
                p.setStock(p.getStock() + line.getQuantity());
                productRepo.save(p);
            }
        }
        inv.setStatus(newStatus);
        return invoiceRepo.save(inv);
    }

    public List<RevenueView> byDay() { return invoiceRepo.revenueByDay(); }
    public List<RevenueView> byMonth() { return invoiceRepo.revenueByMonth(); }
    public List<RevenueView> byYear() { return invoiceRepo.revenueByYear(); }
}
