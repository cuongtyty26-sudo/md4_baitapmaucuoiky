package com.phonestore.service;

import com.phonestore.entity.Customer;
import com.phonestore.exception.ApiException;
import com.phonestore.repository.CustomerRepository;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {
    private final CustomerRepository repo;

    public CustomerService(CustomerRepository repo) { this.repo = repo; }

    public Page<Customer> list(int page, int size) {
        return repo.findAll(PageRequest.of(Math.max(page, 0), Math.max(size, 1), Sort.by("id").descending()));
    }

    public Customer create(Customer c) {
        c.setId(null);
        return repo.save(c);
    }

    public Customer update(Long id, Customer in) {
        Customer c = repo.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy khách hàng"));
        c.setName(in.getName());
        c.setPhone(in.getPhone());
        c.setEmail(in.getEmail());
        c.setAddress(in.getAddress());
        return repo.save(c);
    }

    public void delete(Long id) {
        if (!repo.existsById(id)) throw new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy khách hàng");
        repo.deleteById(id);
        repo.flush();
    }
}
