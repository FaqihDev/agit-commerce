# Product Requirements Document (PRD)
## Order Processing System with Message Broker

**Version:** 1.0.0
**Date:** June 2026
**Status:** Draft

---

## 1. Overview

Order Processing System adalah sistem backend berbasis microservice yang mensimulasikan alur transaksi e-commerce sederhana secara asynchronous menggunakan message broker (RabbitMQ).

**Alur utama sistem:**
```
Customer membuat order → Order diproses → Notifikasi dikirim
```

**Arsitektur:**
```
Client (REST) → Order API (Producer) → RabbitMQ (Topic Exchange)
                                              ↓ order.created
                                       Order Processor (Consumer 1)
                                              ↓ order.paid / order.failed
                                       Notification Consumer (Consumer 2)
                                              ↓
                                       Database Notifications
```

---

## 2. Tech Stack

| Komponen | Teknologi |
|---|---|
| Language | Java 17+ |
| Framework | Spring Boot 3.x |
| Message Broker | RabbitMQ (Topic Exchange) |
| Database | PostgreSQL |
| ORM | Spring Data JPA / Hibernate |
| Migration | Flyway |
| Testing | JUnit 5 + Mockito |
| Build Tool | Maven / Gradle |
| Containerisasi | Docker + Docker Compose |

---

## 3. Functional Requirements

### 3.1 Order API (REST Endpoints)

| Method | Endpoint | Deskripsi |
|---|---|---|
| GET | `/products` | List semua product (support pagination) |
| GET | `/products/{id}` | Detail product + stock |
| POST | `/orders` | Buat order baru |
| GET | `/orders/{id}` | Get order detail + current status |
| GET | `/notifications` | List notifikasi user (query param: `userId`) |

---

### 3.2 Create Order Flow

1. **Validasi input** — `productId` harus exist, `quantity` harus > 0
2. **Cek stock** — stock harus cukup (belum dikurangi di tahap ini)
3. **Simpan order** ke database dengan status `PENDING`
4. **Publish event** `order.created` ke RabbitMQ
5. **Return response** `201 Created` dengan order ID

**Request Body:**
```json
{
  "userId": "user-001",
  "items": [
    { "productId": 1, "quantity": 2 },
    { "productId": 3, "quantity": 1 }
  ]
}
```

**Response:**
```json
{
  "orderId": "uuid-xxx",
  "status": "PENDING",
  "message": "Order created successfully"
}
```

---

### 3.3 Order Processor — Consumer 1

1. Consume message dari queue `order.created`
2. Jika berhasil → update status order ke `PAID`, kurangi stock produk
3. Jika gagal → update status order ke `FAILED`
4. Publish event `order.paid` atau `order.failed` ke RabbitMQ

---

### 3.4 Notification Consumer — Consumer 2

1. Consume event `order.paid` / `order.failed`
2. Simpan notifikasi ke tabel `notifications`
3. Format pesan:
   - Sukses: `"Order #123 berhasil dibayar"`
   - Gagal: `"Order #123 gagal diproses"`

---

### 3.5 Unit Testing

Tambahkan unit test untuk business logic utama:
- **Service layer** — validasi, kalkulasi, status update
- **Consumer** — simulasi consume message sukses & gagal
- **Controller** — request/response handling

---

## 4. Technical Requirements

### 4.1 RabbitMQ

- Gunakan **Topic Exchange** dengan nama `order.ex`
- Routing key yang digunakan:

| Routing Key | Publisher | Consumer |
|---|---|---|
| `order.created` | Order API | Order Processor |
| `order.paid` | Order Processor | Notification Consumer |
| `order.failed` | Order Processor | Notification Consumer |

- Consumer **wajib** menggunakan manual acknowledge (bukan auto-ack)
- Handle consumer error secara graceful — tidak boleh silent fail
- Implementasikan Dead Letter Queue (DLQ) untuk message yang gagal diproses

---

### 4.2 Database — PostgreSQL

**Tabel `products`**

| Kolom | Tipe | Keterangan |
|---|---|---|
| id | BIGSERIAL PRIMARY KEY | Auto increment |
| name | VARCHAR(255) NOT NULL | Nama produk |
| price | NUMERIC(15,2) NOT NULL | Harga produk |
| stock | INT NOT NULL | Jumlah stok |

**Tabel `orders`**

| Kolom | Tipe | Keterangan |
|---|---|---|
| id | UUID PRIMARY KEY | Generated UUID |
| user_id | VARCHAR(100) NOT NULL | ID user |
| total_amount | NUMERIC(15,2) NOT NULL | Total harga order |
| status | VARCHAR(20) NOT NULL | `PENDING` / `PAID` / `FAILED` |
| created_at | TIMESTAMP DEFAULT NOW() | Waktu order dibuat |

**Tabel `order_items`**

| Kolom | Tipe | Keterangan |
|---|---|---|
| id | BIGSERIAL PRIMARY KEY | Auto increment |
| order_id | UUID REFERENCES orders(id) | Foreign key ke orders |
| product_id | BIGINT REFERENCES products(id) | Foreign key ke products |
| quantity | INT NOT NULL | Jumlah item |
| price | NUMERIC(15,2) NOT NULL | Harga saat order dibuat |

**Tabel `notifications`**

| Kolom | Tipe | Keterangan |
|---|---|---|
| id | BIGSERIAL PRIMARY KEY | Auto increment |
| user_id | VARCHAR(100) NOT NULL | ID user penerima |
| message | TEXT NOT NULL | Isi notifikasi |
| is_read | BOOLEAN DEFAULT FALSE | Status baca |
| created_at | TIMESTAMP DEFAULT NOW() | Waktu notifikasi dibuat |

---

### 4.3 Struktur Project

```
order-processing-system/
├── docker-compose.yml
├── src/
│   └── main/
│       ├── java/com/example/order/
│       │   ├── controller/
│       │   │   ├── ProductController.java
│       │   │   ├── OrderController.java
│       │   │   └── NotificationController.java
│       │   ├── service/
│       │   │   ├── ProductService.java
│       │   │   ├── OrderService.java
│       │   │   └── NotificationService.java
│       │   ├── consumer/
│       │   │   ├── OrderProcessorConsumer.java
│       │   │   └── NotificationConsumer.java
│       │   ├── producer/
│       │   │   └── OrderEventProducer.java
│       │   ├── entity/
│       │   │   ├── Product.java
│       │   │   ├── Order.java
│       │   │   ├── OrderItem.java
│       │   │   └── Notification.java
│       │   ├── repository/
│       │   │   ├── ProductRepository.java
│       │   │   ├── OrderRepository.java
│       │   │   ├── OrderItemRepository.java
│       │   │   └── NotificationRepository.java
│       │   ├── dto/
│       │   │   ├── CreateOrderRequest.java
│       │   │   ├── OrderResponse.java
│       │   │   └── OrderEventMessage.java
│       │   └── config/
│       │       └── RabbitMQConfig.java
│       └── resources/
│           ├── application.yml
│           └── db/migration/
│               └── V1__init_schema.sql
└── src/test/
    └── java/com/example/order/
        ├── controller/
        ├── service/
        └── consumer/
```

---

### 4.4 Konfigurasi Utama (`application.yml`)

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/order_db
    username: postgres
    password: password
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
  flyway:
    enabled: true
    locations: classpath:db/migration
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    listener:
      simple:
        acknowledge-mode: manual

rabbitmq:
  exchange: order.ex
  queues:
    order-created: order.created.queue
    order-paid: order.paid.queue
    order-failed: order.failed.queue
  routing-keys:
    order-created: order.created
    order-paid: order.paid
    order-failed: order.failed
```

---

## 5. Order Status Flow

```
[POST /orders]
      ↓
   PENDING
      ↓
 (RabbitMQ: order.created)
      ↓
  Order Processor
   ↙         ↘
PAID        FAILED
  ↓             ↓
(order.paid) (order.failed)
      ↓
 Notification saved
```

---

## 6. Error Handling

| Kondisi | HTTP Status | Pesan |
|---|---|---|
| Product tidak ditemukan | 404 | `Product with id {id} not found` |
| Stock tidak cukup | 400 | `Insufficient stock for product {id}` |
| Quantity <= 0 | 400 | `Quantity must be greater than 0` |
| Order tidak ditemukan | 404 | `Order with id {id} not found` |
| Internal server error | 500 | `Internal server error` |

---

## 7. Docker Compose

Minimal services yang harus ada di `docker-compose.yml`:

```yaml
services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: order_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: password
    ports:
      - "5432:5432"

  rabbitmq:
    image: rabbitmq:3-management
    ports:
      - "5672:5672"
      - "15672:15672"  # RabbitMQ Management UI

  app:
    build: .
    depends_on:
      - postgres
      - rabbitmq
    ports:
      - "8080:8080"
```

---

## 8. Acceptance Criteria

- [ ] Semua REST endpoint berfungsi sesuai spesifikasi
- [ ] Order dibuat dengan status `PENDING` dan event `order.created` berhasil dipublish ke RabbitMQ
- [ ] Order Processor berhasil consume event dan mengubah status ke `PAID` atau `FAILED`
- [ ] Stock produk berkurang setelah order berstatus `PAID`
- [ ] Notifikasi tersimpan di database setelah Consumer 2 menerima event
- [ ] Manual acknowledge diimplementasikan (bukan auto-ack)
- [ ] Error pada consumer tidak menyebabkan silent fail
- [ ] Unit test tersedia untuk service, consumer, dan controller
- [ ] Aplikasi dapat dijalankan via Docker Compose
