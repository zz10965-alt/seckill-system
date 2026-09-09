# Seckill System

An e-commerce flash-sale (seckill) system built with **Spring Boot + Redis + RocketMQ + Sentinel + MySQL**, addressing the core high-concurrency problems: atomic inventory deduction, traffic peak-shaving, rate limiting, distributed locks, and cache preheating.

> **Note**: This is a personal learning project. The core functionality follows the "Seckill System" course by Nine Chapter (九章算法); the package namespace and project structure have been reorganized under a personal namespace.

> **Demo data**: The bundled static frontend includes sample Chinese e-commerce data (a city picker with Chinese place names and Chinese product listings) used for demonstration only.

## Tech Stack

| Component | Purpose |
| --- | --- |
| Spring Boot 2.3.6 / Java 8 | Application framework |
| MySQL + MyBatis | Persistence for orders / users / seckill activities / commodities |
| Redis (Jedis) + Lua | Atomic inventory deduction, cache preheat, distributed locks |
| RocketMQ | Async order / payment message peak-shaving |
| Sentinel | Rate limiting & traffic protection |
| Snowflake | Globally unique ID generation |
| Thymeleaf + static frontend | Page rendering |

## Core Features

- **Inventory deduction**: atomic deduction via Redis + Lua scripts to prevent overselling (`SeckillOverSellService`).
- **Async order / payment**: orders and payments decoupled through RocketMQ messages (`mq/`).
- **Cache preheating**: activities and inventory preheated into Redis on startup (`RedisPreheatRunner`).
- **Distributed locks**: Redis-based concurrency safety (`util/RedisService`).
- **Rate limiting**: Sentinel integration on core endpoints.
- **Page caching**: dynamic generation and caching of activity detail pages (`ActivityHtmlPageService`).
- **Global IDs**: Snowflake algorithm (`util/SnowFlake`).

## Project Structure

```
src/main/java/com/zhixian/seckill/
├── SeckillApplication.java      # Entry point
├── component/                   # Redis preheat runner
├── config/                      # Jedis config
├── db/                          # MyBatis DAO / Mapper / PO
├── mq/                          # RocketMQ consumers & message service
├── service/                     # Seckill business / anti-oversell / page cache
├── util/                        # Redis util, Snowflake
└── web/                         # Controllers
src/main/resources/
├── mappers/                     # MyBatis XML
├── templates/                   # Thymeleaf pages
├── public/                      # Static assets (CSS/JS/img)
└── application.properties       # Configuration
```

## Getting Started

### Prerequisites

- JDK 8
- Maven 3.x
- MySQL 5.7+ (create database `seckill_db`)
- Redis
- RocketMQ

### Configuration

Edit `src/main/resources/application.properties` with your DB / Redis / RocketMQ connection info:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/seckill_db?...
spring.redis.host=localhost
rocketmq.name-server=localhost:9876
```

### Run

```bash
mvn spring-boot:run
```

Open `http://localhost:8080`.

## License

[Apache License 2.0](LICENSE) for the project skeleton; the seckill business code is a personal learning implementation, free to use with attribution.
