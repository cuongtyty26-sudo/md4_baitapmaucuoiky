package com.phonestore.controller;

import com.phonestore.entity.Product;
import com.phonestore.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService service;

    public ProductController(ProductService service) { this.service = service; }

    @GetMapping
    public Page<Product> list(@RequestParam(required = false) String brand,
                              @RequestParam(required = false) Long minPrice,
                              @RequestParam(required = false) Long maxPrice,
                              @RequestParam(required = false) Integer stock,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size,
                              @RequestParam(defaultValue = "id") String sortBy,
                              @RequestParam(defaultValue = "asc") String dir) {
        return service.search(brand, minPrice, maxPrice, stock, page, size, sortBy, dir);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Product create(@Valid @RequestBody Product p) { return service.create(p); }

    @PutMapping("/{id}")
    public Product update(@PathVariable Long id, @Valid @RequestBody Product p) { return service.update(id, p); }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }
}
