# Agit Commerce - Order Processing System

## 1. Overview
Aplikasi ini adalah sebuah **Order Processing System** (Sistem Pemrosesan Pesanan) berbasis _microservices_ yang dirancang untuk menangani transaksi pemesanan pada platform _e-commerce_ (Agit Commerce). Sistem ini bertanggung jawab untuk menerima permintaan pesanan (*order*), melakukan validasi terhadap ketersediaan stok produk secara waktu nyata (*real-time*), serta mengamankan dan mencatat proses transaksi hingga mempublikasikan *event* notifikasi kepada pembeli.

Sistem ini didesain dengan prinsip **Clean Code** dan arsitektur *event-driven* guna menjamin skalabilitas (*scalability*), ketahanan (*resilience*), dan pemisahan logika yang baik.

## 2. Flow Aplikasi
Proses inti dari berjalannya aplikasi ini tergambar dalam alur pemesanan produk berikut:

1. **Inisialisasi Order (Order Creation)**
   - Pengguna mengirimkan *request* pemesanan melalui API `POST /orders`.
   - Sistem akan langsung **mengecek ketersediaan stok produk** secara sinkronus (*synchronous*).
   - Jika stok tidak mencukupi, sistem seketika membatalkan transaksi dan memberikan respon JSON berstatus *error* (kode 204) (tanpa menyimpan data ke database).
   - Jika stok memadai, sistem akan membuat data pesanan (`Order`) dengan status `PENDING`, lalu menyimpannya ke dalam database.
2. **Event Publishing (Memasukkan ke Antrean)**
   - Setelah pesanan awal disimpan, sistem akan mengirim *message* antrean (berisi `orderId`) ke dalam **RabbitMQ**.
3. **Asynchronous Processing (Pemrosesan Latar Belakang)**
   - *Worker/Consumer* (`OrderProcessorConsumer`) akan membaca pesan dari antrean RabbitMQ secara asinkron.
   - *Worker* akan melakukan verifikasi dan memproses pemotongan stok pada produk terkait (*deduct stock*).
   - Status pesanan kemudian diperbarui dari `PENDING` menjadi `PAID` (jika sukses) atau `FAILED` (jika gagal).
   - *Worker* lalu mempublikasikan ulang status terbaru pesanan ini ke RabbitMQ (`order.paid` / `order.failed`).
4. **Notifikasi (Notification Handling)**
   - `NotificationConsumer` akan menangkap *event* status terbaru tersebut dan membuat histori **Notifikasi** untuk *User*.
   - Histori notifikasi kemudian dapat dilihat oleh *User* melalui API `GET /notifications`.

## 3. Tech Stack yang Digunakan
Sistem ini dibangun dengan tumpukan teknologi berikut:

- **Java 17**: Bahasa pemrograman utama.
- **Spring Boot 3**: *Framework* backend REST API.
- **Spring Data JPA & Hibernate**: Pengelolaan ORM (Object-Relational Mapping).
- **PostgreSQL**: Relational Database Management System.
- **Flyway**: Kontrol versi untuk migrasi struktur *database*.
- **RabbitMQ**: *Message Broker* untuk antrean dan arsitektur *event-driven*.
- **Lombok**: Pustaka untuk otomatisasi kode *boilerplate* (otomatis *generate* Getter/Setter/Constructor).
- **Maven**: Alat manajemen dependensi dan eksekusi kompilasi.
- **Docker & Docker Compose**: Orkestrasi kontainer secara lokal untuk RabbitMQ dan PostgreSQL.
