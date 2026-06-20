# AutoMagazin — запуск проекта

## Требования

- **Java 17** (проверить: `java -version`)
  - Windows: `winget install Microsoft.OpenJDK.17`
  - Другие ОС: скачать с https://adoptium.net/

## Быстрый старт (dev-профиль — без PostgreSQL и Redis)

Самый простой способ — все данные хранятся в H2 file-based БД.

```bash
cd AutoMagazin

# Windows
.\mvnw spring-boot:run "-Dspring-boot.run.profiles=dev"

# macOS / Linux
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Приложение будет доступно по адресу: **http://localhost:8080**

### H2 Console (веб-интерфейс БД)

- **URL**: http://localhost:8080/h2-console
- **JDBC URL**: `jdbc:h2:file:./data/devdb`
- **User**: `sa`
- **Password**: (пусто)

## Полный запуск (PostgreSQL + Redis — для продакшна)

### Настройка окружения

Создать переменные окружения:

```bash
# Windows (PowerShell)
$env:DB_PASSWORD = "your_password"
$env:JWT_SECRET = "your_jwt_secret_key_min_256_bits"

# macOS / Linux
export DB_PASSWORD="your_password"
export JWT_SECRET="your_jwt_secret_key_min_256_bits"
```

### Запуск PostgreSQL и Redis

```bash
# Через Docker (рекомендуется)
docker run -d --name postgres -e POSTGRES_DB=test -e POSTGRES_PASSWORD=your_password -p 5432:5432 postgres:17
docker run -d --name redis -p 6379:6379 redis:7
```

### Запуск приложения

```bash
cd AutoMagazin
.\mvnw spring-boot:run
```

## API Endpoints

### Auth (`/auth`) — публичные

| Метод  | Путь            | Описание                     |
|--------|-----------------|------------------------------|
| POST   | `/auth/register` | Регистрация пользователя     |
| POST   | `/auth/login`    | Вход, получение токенов     |
| POST   | `/auth/refresh`  | Обновление access-токена    |
| GET    | `/auth/me`       | Текущий пользователь (JWT)  |

### Cars (`/api/cars`)

| Метод  | Путь              | Описание                        |
|--------|-------------------|----------------------------------|
| GET    | `/api/cars`       | Список всех машин                |
| POST   | `/api/cars`       | Создать машину (ADMIN)           |
| PUT    | `/api/cars/{id}`  | Обновить машину (ADMIN)          |
| PATCH  | `/api/cars/{id}`  | Частичное обновление (ADMIN)    |
| DELETE | `/api/cars/{id}`  | Удалить машину (ADMIN)          |

### Users (`/users`)

| Метод  | Путь                      | Описание                        |
|--------|---------------------------|----------------------------------|
| GET    | `/users`                  | Список пользователей (ADMIN)    |
| GET    | `/users/{id}`             | Пользователь по ID              |
| PUT    | `/users/{id}/password`    | Сменить пароль                  |
| PUT    | `/users/{id}/personal-data`| Обновить личные данные         |
| PUT    | `/users/{id}/birth-date`  | Обновить дату рождения          |
| POST   | `/users/email`            | Установить email                |
| PATCH  | `/users/{id}/role`        | Сменить роль (ADMIN)            |
| DELETE | `/users/{id}`             | Удалить пользователя (ADMIN)    |

### Swagger UI

- **URL**: http://localhost:8080/swagger-ui/index.html
- **API docs**: http://localhost:8080/v3/api-docs
