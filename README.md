# 🏥 Clinic Backend API

REST API cho hệ thống đặt lịch khám bệnh, xây dựng với Spring Boot.

**Base URL (production):** `https://clinic-backend-nvos.onrender.com/api/v1`

---

## Tính năng

- Xác thực JWT (đăng ký, đăng nhập)
- Quản lý bác sĩ, lịch làm việc
- Đặt lịch, hủy lịch, hoàn thành khám
- Xử lý race condition bằng Pessimistic Locking
- Phân trang, filter theo chuyên khoa

---

## Tech stack

| | |
|---|---|
| Framework | Spring Boot 3.x |
| Security | Spring Security + JWT (jjwt 0.11.5) |
| ORM | Spring Data JPA + Hibernate |
| Database | MySQL 8 |
| Deploy | Render |
| Build | Maven |

---

## Cấu trúc project

```
src/main/java/com/example/demo/
├── controller/
│   ├── AuthController.java       # POST /auth/login, /auth/register
│   ├── DoctorController.java     # GET /doctors
│   ├── ScheduleController.java   # GET /doctors/{id}/schedules
│   └── BookingController.java    # CRUD bookings
├── service/
│   ├── AuthService.java
│   ├── DoctorService.java
│   ├── ScheduleService.java
│   └── BookingService.java       # Business logic + Pessimistic Lock
├── repository/
│   ├── UserRepository.java       # findByIdWithLock (PESSIMISTIC_WRITE)
│   ├── BookingRepository.java    # existsConflict, findBookedTimes
│   ├── DoctorProfileRepository.java
│   └── ScheduleRepository.java
├── entity/
│   ├── User.java
│   ├── Booking.java
│   ├── Schedule.java
│   └── DoctorProfile.java
├── dto/
│   ├── BookingRequest.java
│   ├── BookingResponse.java
│   ├── LoginRequest.java / LoginResponse.java
│   ├── RegisterRequest.java
│   ├── DoctorResponse.java
│   ├── ScheduleResponse.java
│   └── PageResponse.java
├── enums/
│   ├── Role.java                 # PATIENT, DOCTOR, ADMIN
│   └── BookingStatus.java        # BOOKED, CANCELLED, DONE
├── exception/
│   ├── AppException.java
│   ├── ErrorCode.java
│   └── GlobalExceptionHandler.java
├── config/
│   ├── SecurityConfig.java       # Filter chain + CORS
│   └── JwtAuthFilter.java        # Parse JWT mỗi request
└── util/
    └── JwtUtil.java              # Generate + parse JWT
```

---

## Chạy local

**Yêu cầu:** Java 17+, MySQL 8, Maven

**1. Tạo database:**

```sql
CREATE DATABASE clinic_booking;
```

**2. Cấu hình `src/main/resources/application.properties`:**

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/clinic_booking
spring.datasource.username=root
spring.datasource.password=your_password

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

app.jwt.secret=your-secret-key-at-least-32-characters-long
app.jwt.expiration-ms=86400000
```

**3. Chạy:**

```bash
mvn spring-boot:run
# → http://localhost:8080
```

**4. Import dữ liệu mẫu:**

```sql
-- Chạy file schema.sql trong /docs hoặc xem README Database bên dưới
```

---

## API Endpoints

### Auth
```
POST /api/v1/auth/register    Đăng ký tài khoản bệnh nhân
POST /api/v1/auth/login       Đăng nhập, trả về JWT token
```

### Doctors
```
GET  /api/v1/doctors                      Danh sách bác sĩ (phân trang, filter)
GET  /api/v1/doctors?specialty=tim        Filter theo chuyên khoa
GET  /api/v1/doctors?page=0&size=10       Phân trang
GET  /api/v1/doctors/{id}/schedules       Lịch làm việc của bác sĩ
```

### Bookings (cần Bearer token)
```
POST  /api/v1/bookings                      Đặt lịch khám
GET   /api/v1/bookings/my                   Lịch của bệnh nhân đang đăng nhập
PATCH /api/v1/bookings/{id}/cancel          Hủy lịch
GET   /api/v1/bookings/doctor               Lịch của bác sĩ đang đăng nhập
PATCH /api/v1/bookings/{id}/done            Đánh dấu hoàn thành (bác sĩ)
GET   /api/v1/bookings/doctor/{id}/slots    Các slot đã đặt trong ngày
        ?date=2026-05-01                    → trả [8, 10, 14]
```

### Request mẫu

**Đăng nhập:**
```json
POST /api/v1/auth/login
{
  "username": "benhnhan1",
  "password": "123456"
}
```

**Đặt lịch:**
```json
POST /api/v1/bookings
Authorization: Bearer <token>
{
  "doctorId": 3,
  "startTime": "2026-05-10T08:00:00",
  "endTime": "2026-05-10T09:00:00",
  "note": "Khám đau ngực"
}
```

---

## Database

```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,       -- BCrypt hash
    full_name VARCHAR(100),
    phone VARCHAR(20),
    role ENUM('PATIENT', 'DOCTOR', 'ADMIN') NOT NULL,
    status TINYINT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE doctor_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    specialty VARCHAR(100),
    description TEXT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE schedules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    doctor_id BIGINT NOT NULL,
    work_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    FOREIGN KEY (doctor_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE bookings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    status ENUM('BOOKED', 'CANCELLED', 'DONE') DEFAULT 'BOOKED',
    note TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_booking_doctor_time (doctor_id, start_time, end_time),
    INDEX idx_booking_patient (patient_id)
);
```

---

## Xử lý Race Condition

Khi 2 bệnh nhân đặt cùng slot của cùng 1 bác sĩ:

```
User A ──┐
          ├── cùng đặt doctor #3, 08:00–09:00
User B ──┘

① Cả 2 vào createBooking()
② Cả 2 gọi findByIdWithLock(doctorId)
   → DB lock row doctor, User B BLOCK

③ User A: check conflict → false → save() → commit → RELEASE LOCK
④ User B: tiếp tục, check conflict → TRUE → 409 TIME_SLOT_TAKEN
```

Dùng `@Lock(LockModeType.PESSIMISTIC_WRITE)` + `@Transactional` trong `BookingService`.

---

## Deploy lên Render

1. Push code lên GitHub
2. Tạo **Web Service** trên [render.com](https://render.com)
3. Cấu hình Environment Variables:

```
SPRING_DATASOURCE_URL=jdbc:mysql://your-db-host:3306/clinic_booking
SPRING_DATASOURCE_USERNAME=your_user
SPRING_DATASOURCE_PASSWORD=your_password
APP_JWT_SECRET=your-secret-key-at-least-32-characters
APP_JWT_EXPIRATION_MS=86400000
```

4. Build command: `mvn clean package -DskipTests`
5. Start command: `java -jar target/demo-0.0.1-SNAPSHOT.jar`

> ⚠️ Free tier Render sẽ sleep sau 15 phút không có request, lần đầu gọi có thể mất 30–60 giây để wake up.
