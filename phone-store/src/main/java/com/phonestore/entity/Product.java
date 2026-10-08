package com.phonestore.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "product")
@Getter @Setter
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 150, message = "Tên sản phẩm tối đa 150 ký tự")
    private String name;

    @NotBlank(message = "Nhãn hàng không được để trống")
    @Size(max = 80, message = "Nhãn hàng tối đa 80 ký tự")
    private String brand;

    @NotNull(message = "Giá không được để trống")
    @Min(value = 0, message = "Giá phải >= 0")
    private Long price;

    @NotNull(message = "Tồn kho không được để trống")
    @Min(value = 0, message = "Tồn kho phải >= 0")
    private Integer stock;

    @Size(max = 500, message = "Mô tả tối đa 500 ký tự")
    private String description;
}
