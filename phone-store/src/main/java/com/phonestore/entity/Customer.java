package com.phonestore.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "customer")
@Getter @Setter
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tên khách hàng không được để trống")
    @Size(max = 100, message = "Tên tối đa 100 ký tự")
    private String name;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^0\\d{9}$", message = "Số điện thoại phải gồm 10 chữ số và bắt đầu bằng 0")
    private String phone;

    @Email(message = "Email không hợp lệ")
    private String email;

    @Size(max = 255, message = "Địa chỉ tối đa 255 ký tự")
    private String address;
}
