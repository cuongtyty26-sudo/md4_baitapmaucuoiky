# Quản lý cửa hàng điện thoại (Spring Boot + MySQL + REST API)

## Chạy dự án
1. Cài JDK 17+, Maven, MySQL (mở bằng MySQL Workbench để xem dữ liệu).
2. Sửa `src/main/resources/application.properties`: username/password MySQL.
   (Database `phone_store` và các bảng tự được tạo khi chạy lần đầu.)
3. Chạy: `mvn spring-boot:run` (hoặc chạy class `PhoneStoreApplication` trong IntelliJ/Eclipse).
   IDE cần cài plugin Lombok.
4. Mở http://localhost:8080 -> bấm "Đăng ký" tạo admin -> đăng nhập.

## API
| Nhóm | Endpoint |
|---|---|
| Auth | POST /api/auth/register, /login, /logout; GET /api/auth/me |
| Sản phẩm | GET /api/products?brand=&minPrice=&maxPrice=&stock=&page=&size=&sortBy=&dir= ; POST ; PUT /{id} ; DELETE /{id} |
| Khách hàng | GET /api/customers?page=&size= ; POST ; PUT /{id} ; DELETE /{id} |
| Hóa đơn | GET /api/invoices?keyword=&page=&size= ; POST ; PATCH /{id}/status |
| Doanh thu | GET /api/statistics/daily, /monthly, /yearly |
