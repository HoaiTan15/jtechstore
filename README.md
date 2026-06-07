# J-Tech Store

J-Tech Store là website kinh doanh thiết bị công nghệ được xây dựng bằng Java Spring Boot. Hệ thống hỗ trợ khách hàng xem sản phẩm, tìm kiếm/lọc sản phẩm, thêm giỏ hàng, đặt hàng, thanh toán COD/QR chuyển khoản, đánh giá sản phẩm sau khi mua hàng. Phân hệ admin hỗ trợ quản lý danh mục, sản phẩm, tồn kho, khuyến mãi, đơn hàng, khách hàng, đánh giá và dashboard thống kê.

## 1. Công nghệ sử dụng

- Java 21
- Spring Boot
- Spring MVC
- Spring Data JPA / Hibernate
- Thymeleaf
- Bootstrap 5
- SQL Server
- Maven
- Cloudflared Tunnel để chia sẻ web tạm thời

## 2. Chức năng chính

### Khách hàng

- Đăng ký tài khoản
- Đăng nhập, đăng xuất
- Nhớ tài khoản khi đăng nhập
- Quên mật khẩu qua email
- Xem trang chủ
- Xem danh sách sản phẩm
- Tìm kiếm sản phẩm theo tên
- Lọc sản phẩm theo danh mục, thương hiệu, khoảng giá
- Sắp xếp sản phẩm theo giá hoặc tên
- Xem chi tiết sản phẩm
- Thêm sản phẩm vào giỏ hàng
- Cập nhật số lượng trong giỏ hàng
- Xóa sản phẩm khỏi giỏ hàng
- Đặt hàng
- Thanh toán khi nhận hàng
- Thanh toán chuyển khoản bằng mã QR
- Xem lịch sử đơn hàng
- Xem chi tiết đơn hàng
- Hủy đơn hàng khi đơn còn ở trạng thái chờ xác nhận
- Đánh giá sản phẩm sau khi mua và thanh toán

### Admin

- Đăng nhập tài khoản admin
- Dashboard thống kê
- Quản lý danh mục
- Quản lý sản phẩm
- Quản lý tồn kho
- Cảnh báo sản phẩm sắp hết hàng / hết hàng
- Quản lý khuyến mãi và coupon
- Quản lý đơn hàng
- Xác nhận đơn hàng
- Xác nhận thanh toán
- Cập nhật trạng thái giao hàng
- Hủy đơn hàng và hoàn lại tồn kho
- Quản lý tài khoản khách hàng
- Khóa / mở khóa tài khoản
- Quản lý đánh giá sản phẩm
- Phân quyền chặn user thường truy cập `/admin/**`

## 3. Yêu cầu môi trường

Cần cài đặt:

- JDK 21
- Maven
- SQL Server
- IntelliJ IDEA hoặc IDE hỗ trợ Spring Boot
- Cloudflared nếu muốn chia sẻ web public tạm thời

Kiểm tra Java:

```bash
java -version
```

Kiểm tra Maven:

```bash
mvn -version
```

## 4. Cấu hình cơ sở dữ liệu

Tạo database trong SQL Server:

```sql
CREATE DATABASE JTechStore;
```

Thông tin mặc định trong `application.properties`:

```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=JTechStore;encrypt=true;trustServerCertificate=true
spring.datasource.username=sa
spring.datasource.password=123
```

Có thể đổi tài khoản SQL Server bằng biến môi trường:

```properties
DB_URL
DB_USERNAME
DB_PASSWORD
```

Ví dụ:

```properties
spring.datasource.url=${DB_URL:jdbc:sqlserver://localhost:1433;databaseName=JTechStore;encrypt=true;trustServerCertificate=true}
spring.datasource.username=${DB_USERNAME:sa}
spring.datasource.password=${DB_PASSWORD:123}
```

## 5. Cấu hình email quên mật khẩu

Project dùng Gmail SMTP để gửi mã xác nhận quên mật khẩu.

Trong `application.properties` nên dùng biến môi trường:

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${MAIL_USERNAME:}
spring.mail.password=${MAIL_PASSWORD:}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

app.mail.from=${MAIL_USERNAME:}
```

Khi chạy bằng PowerShell:

```powershell
$env:MAIL_USERNAME="email_cua_ban@gmail.com"
$env:MAIL_PASSWORD="mat_khau_ung_dung_gmail"
mvn spring-boot:run
```

Lưu ý: không đưa mật khẩu Gmail thật lên GitHub.

## 6. Cách chạy project

Mở terminal tại thư mục gốc project:

```bash
cd jtechstore
```

Chạy project bằng Maven:

```bash
mvn spring-boot:run
```

Hoặc chạy trực tiếp trong IntelliJ bằng class:

```text
JtechstoreApplication
```

Sau khi chạy thành công, mở trình duyệt:

```text
http://localhost:8080
```

## 7. Tài khoản đăng nhập demo

### Admin

```text
Username: admin
Password: 123456
```

### Khách hàng

Có thể đăng ký tài khoản mới tại:

```text
http://localhost:8080/register
```

## 8. Một số đường dẫn chính

### Khách hàng

```text
http://localhost:8080/
http://localhost:8080/products
http://localhost:8080/cart
http://localhost:8080/login
http://localhost:8080/register
http://localhost:8080/tai-khoan
```

### Admin

```text
http://localhost:8080/admin/dashboard
http://localhost:8080/admin/categories
http://localhost:8080/admin/products
http://localhost:8080/admin/products/inventory
http://localhost:8080/admin/promotions
http://localhost:8080/admin/orders
http://localhost:8080/admin/users
http://localhost:8080/admin/reviews
```

## 9. Chạy public bằng Cloudflared

Sau khi Spring Boot đang chạy ở port 8080, mở terminal mới và chạy:

```bash
cloudflared tunnel --url http://localhost:8080
```

Cloudflared sẽ tạo link dạng:

```text
https://ten-ngau-nhien.trycloudflare.com
```

Copy link này để gửi người khác test website.

## 10. Cấu trúc thư mục chính

```text
src
└── main
    ├── java
    │   └── com.jtech.jtechstore
    │       ├── config
    │       ├── controller
    │       ├── model
    │       ├── repository
    │       └── service
    └── resources
        ├── static
        │   ├── css
        │   └── images
        ├── templates
        │   ├── admin
        │   ├── customer
        │   └── *.html
        └── application.properties
```

## 11. Ghi chú khi chạy

Nếu ảnh sản phẩm không hiển thị, kiểm tra thư mục:

```text
uploads/products
```

Nếu không gửi được email quên mật khẩu, kiểm tra:

- Đã cấu hình `MAIL_USERNAME`
- Đã cấu hình `MAIL_PASSWORD`
- Gmail phải dùng App Password, không dùng mật khẩu tài khoản thường

Nếu không kết nối được database, kiểm tra:

- SQL Server đã bật
- Port 1433 đã mở
- Database `JTechStore` đã tồn tại
- Username/password SQL Server đúng

## 12. Lệnh Git thường dùng

Kiểm tra trạng thái:

```bash
git status
```

Thêm toàn bộ file:

```bash
git add .
```

Commit:

```bash
git commit -m "Update J-Tech Store project"
```

Push lên GitHub:

```bash
git push origin main
```

## 13. Tác giả

Sinh viên thực hiện: Phan Hoài Tân

Đề tài: Website kinh doanh thiết bị công nghệ J-Tech Store
