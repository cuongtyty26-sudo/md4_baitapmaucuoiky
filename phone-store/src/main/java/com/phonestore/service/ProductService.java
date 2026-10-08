package com.phonestore.service;

import com.phonestore.entity.Product;
import com.phonestore.exception.ApiException;
import com.phonestore.repository.ProductRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {
    private final ProductRepository repo;

    public ProductService(ProductRepository repo) { this.repo = repo; }

    public Page<Product> search(String brand, Long minPrice, Long maxPrice, Integer stock,
                                int page, int size, String sortBy, String dir) {
        Specification<Product> spec = (root, q, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            if (brand != null && !brand.isBlank())
                ps.add(cb.like(cb.lower(root.get("brand")), "%" + brand.trim().toLowerCase() + "%"));
            if (minPrice != null) ps.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            if (maxPrice != null) ps.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            if (stock != null) ps.add(cb.lessThanOrEqualTo(root.get("stock"), stock)); // tồn kho <= n
            return cb.and(ps.toArray(new Predicate[0]));
        };
        List<String> allowed = List.of("id", "name", "brand", "price", "stock");
        String field = allowed.contains(sortBy) ? sortBy : "id";
        Sort sort = "desc".equalsIgnoreCase(dir) ? Sort.by(field).descending() : Sort.by(field).ascending();
        return repo.findAll(spec, PageRequest.of(Math.max(page, 0), Math.max(size, 1), sort));
    }

    public Product create(Product p) {
        p.setId(null);
        return repo.save(p);
    }

    public Product update(Long id, Product in) {
        Product p = repo.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm"));
        p.setName(in.getName());
        p.setBrand(in.getBrand());
        p.setPrice(in.getPrice());
        p.setStock(in.getStock());
        p.setDescription(in.getDescription());
        return repo.save(p); // id giữ nguyên
    }

    public void delete(Long id) {
        if (!repo.existsById(id)) throw new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm");
        repo.deleteById(id);
        repo.flush();
    }
}
