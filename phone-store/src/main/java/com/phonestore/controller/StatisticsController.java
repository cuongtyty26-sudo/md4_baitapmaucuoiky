package com.phonestore.controller;

import com.phonestore.repository.RevenueView;
import com.phonestore.service.InvoiceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {
    private final InvoiceService service;

    public StatisticsController(InvoiceService service) { this.service = service; }

    @GetMapping("/daily")
    public List<Map<String, Object>> daily() { return map(service.byDay()); }

    @GetMapping("/monthly")
    public List<Map<String, Object>> monthly() { return map(service.byMonth()); }

    @GetMapping("/yearly")
    public List<Map<String, Object>> yearly() { return map(service.byYear()); }

    private List<Map<String, Object>> map(List<RevenueView> rows) {
        return rows.stream()
                .map(r -> Map.<String, Object>of("period", r.getPeriod(), "revenue", r.getRevenue(), "orders", r.getOrders()))
                .toList();
    }
}
