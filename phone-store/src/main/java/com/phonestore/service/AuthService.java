package com.phonestore.service;

import com.phonestore.dto.LoginRequest;
import com.phonestore.dto.RegisterRequest;
import com.phonestore.entity.Admin;
import com.phonestore.exception.ApiException;
import com.phonestore.repository.AdminRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final AdminRepository repo;
    private final PasswordEncoder encoder;

    public AuthService(AdminRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    public void register(RegisterRequest r) {
        if (repo.existsByUsername(r.username()))
            throw new ApiException(HttpStatus.CONFLICT, "Username đã tồn tại");
        Admin a = new Admin();
        a.setUsername(r.username());
        a.setPassword(encoder.encode(r.password()));
        repo.save(a);
    }

    public Admin login(LoginRequest r) {
        Admin a = repo.findByUsername(r.username())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Sai username hoặc password"));
        if (!encoder.matches(r.password(), a.getPassword()))
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Sai username hoặc password");
        return a;
    }
}
