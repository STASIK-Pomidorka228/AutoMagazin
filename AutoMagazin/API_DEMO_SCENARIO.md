# Сценарий ручного тестирования AutoMagazin в Postman

## Подготовка

1. Запустить приложение:
   ```powershell
   cd AutoMagazin
   $env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.19.10-hotspot"
   .\mvnw spring-boot:run "-Dspring-boot.run.profiles=dev"
   ```

2. Импортировать коллекцию в Postman:
   - `File → Import → Выбрать файл postman_collection.json`
   - Переменные подставятся автоматически

3. База уже содержит 2 демо-пользователей:
   | Логин | Пароль | Роль |
   |-------|--------|------|
   | admin | admin123 | ADMIN |
   | user | user123 | USER |

4. В каталоге 9 предустановленных автомобилей (Renault, Dacia, Ford, Skoda, Audi)

---

## Часть 1. Регистрация и аутентификация (публичные эндпоинты)

### 1.1 Регистрация нового пользователя
- **Метод:** `POST`
- **URL:** `http://localhost:8080/auth/register`
- **Headers:** `Content-Type: application/json`
- **Body:**
  ```json
  {
    "username": "presentation",
    "lastName": "Testerson",
    "email": "present@automagazin.ru",
    "password": "pass123"
  }
  ```
- **Ожидаемый результат:** `200 OK`, приходят accessToken + refreshToken + данные пользователя
- **Что показать:** Автоматическое создание учётной записи и выдача JWT-токенов

### 1.2 Вход обычного пользователя
- **Метод:** `POST`
- **URL:** `http://localhost:8080/auth/login`
- **Body:**
  ```json
  {
    "username": "user",
    "password": "user123"
  }
  ```
- **Ожидаемый результат:** `200 OK`, токены + информация о пользователе (роль USER)
- **Что показать:** Вход в систему, получение JWT, роль USER

### 1.3 Вход администратора
- **Метод:** `POST`
- **URL:** `http://localhost:8080/auth/login`
- **Body:**
  ```json
  {
    "username": "admin",
    "password": "admin123"
  }
  ```
- **Ожидаемый результат:** `200 OK`, токены + роль ADMIN
- **Что показать:** Вход с ролью ADMIN, отличие от USER

### 1.4 Профиль текущего пользователя
- **Метод:** `GET`
- **URL:** `http://localhost:8080/auth/me`
- **Headers:** `Authorization: Bearer {{token}}`
- **Ожидаемый результат:** `200 OK`, данные пользователя
- **Что показать:** Запрос к защищённому эндпоинту с JWT

### 1.5 Обновление токенов
- **Метод:** `POST`
- **URL:** `http://localhost:8080/auth/refresh`
- **Body:**
  ```json
  {
    "accessToken": "{{accessToken}}",
    "refreshToken": "{{refreshToken}}"
  }
  ```
- **Ожидаемый результат:** `200 OK`, новые токены
- **Что показать:** Механизм refresh-токенов (access = 15 мин, refresh = 7 дней)

---

## Часть 2. Работа с каталогом автомобилей (JWT required)

### 2.1 Список всех автомобилей
- **Метод:** `GET`
- **URL:** `http://localhost:8080/api/cars`
- **Headers:** `Authorization: Bearer {{token}}`
- **Ожидаемый результат:** `200 OK`, массив из 9 автомобилей
- **Что показать:** Каталог машин, доступный любому аутентифицированному пользователю

### 2.2 Создание автомобиля (ADMIN only)
- **Метод:** `POST`
- **URL:** `http://localhost:8080/api/cars`
- **Headers:** `Authorization: Bearer {{token_админа}}`
- **Body:**
  ```json
  {
    "brand": "Tesla",
    "model": "Model 3",
    "name": "Tesla Model 3",
    "price": 55000,
    "volume": 0.0,
    "power": 450
  }
  ```
- **Ожидаемый результат:** `200 OK`, объект машины с присвоенным ID (10)
- **Что показать:** Создание записи — только администратором, авто-ID

### 2.3 Полное обновление (PUT, ADMIN only)
- **Метод:** `PUT`
- **URL:** `http://localhost:8080/api/cars/10`
- **Headers:** `Authorization: Bearer {{token_админа}}`
- **Body:**
  ```json
  {
    "brand": "Tesla",
    "model": "Model 3 Performance",
    "name": "Tesla Model 3 Performance",
    "price": 65000,
    "volume": 0.0,
    "power": 510
  }
  ```
- **Ожидаемый результат:** `200 OK`, данные обновлены полностью
- **Что показать:** PUT — заменяет весь объект

### 2.4 Частичное обновление (PATCH, ADMIN only)
- **Метод:** `PATCH`
- **URL:** `http://localhost:8080/api/cars/10`
- **Headers:** `Authorization: Bearer {{token_админа}}`
- **Body:**
  ```json
  {
    "price": 59000
  }
  ```
- **Ожидаемый результат:** `200 OK`, цена изменилась, остальные поля без изменений
- **Что показать:** PATCH — обновляет только указанные поля

### 2.5 Удаление автомобиля (ADMIN only)
- **Метод:** `DELETE`
- **URL:** `http://localhost:8080/api/cars/10`
- **Headers:** `Authorization: Bearer {{token_админа}}`
- **Ожидаемый результат:** `200 OK`, пустое тело
- **Что показать:** Удаление записи, затем GET /api/cars — снова 9 машин

---

## Часть 3. Управление пользователями

### 3.1 Список всех пользователей (ADMIN only)
- **Метод:** `GET`
- **URL:** `http://localhost:8080/users`
- **Headers:** `Authorization: Bearer {{token_админа}}`
- **Ожидаемый результат:** `200 OK`, массив всех пользователей с ролями
- **Что показать:** Панель администратора — полный список пользователей

### 3.2 Получение пользователя по ID
- **Метод:** `GET`
- **URL:** `http://localhost:8080/users/2`
- **Headers:** `Authorization: Bearer {{token}}`
- **Ожидаемый результат:** `200 OK`, данные пользователя с ID=2
- **Что показать:** Детальная информация о конкретном пользователе

### 3.3 Обновление личных данных
- **Метод:** `PUT`
- **URL:** `http://localhost:8080/users/{{userId}}/personal-data`
- **Headers:** `Authorization: Bearer {{token}}`, `Content-Type: application/json`
- **Body:**
  ```json
  {
    "firstName": "Peter",
    "lastName": "Peterson-Updated",
    "birthDate": "1995-06-20"
  }
  ```
- **Ожидаемый результат:** `200 OK`, затем GET /auth/me подтверждает изменения
- **Что показать:** Редактирование профиля пользователя

### 3.4 Смена пароля
- **Метод:** `PUT`
- **URL:** `http://localhost:8080/users/{{userId}}/password`
- **Body:**
  ```json
  {
    "password": "newpass123"
  }
  ```
- **Ожидаемый результат:** `200 OK`, затем вход с новым паролем
- **Что показать:** Смена пароля; вход со старым паролем вернёт ошибку

### 3.5 Изменение роли пользователя (ADMIN only)
- **Метод:** `PATCH`
- **URL:** `http://localhost:8080/users/2/role`
- **Headers:** `Authorization: Bearer {{token_админа}}`
- **Body:**
  ```json
  {
    "role": "ADMIN"
  }
  ```
- **Ожидаемый результат:** `200 OK`, затем GET /users/2 — роль ADMIN
- **Что показать:** Назначение прав администратора → вернуть обратно USER

### 3.6 Удаление пользователя (ADMIN only)
- **Метод:** `DELETE`
- **URL:** `http://localhost:8080/users/3`
- **Headers:** `Authorization: Bearer {{token_админа}}`
- **Ожидаемый результат:** `200 OK`
- **Что показать:** Удаление тестового пользователя

---

## Часть 4. Дополнительно

### Swagger UI
- **URL:** http://localhost:8080/swagger-ui/index.html
- Нажать `Authorize` и ввести JWT токен для тестирования эндпоинтов
- Показать интерактивную документацию

### H2 Console
- **URL:** http://localhost:8080/h2-console
- **JDBC URL:** `jdbc:h2:file:./data/devdb`
- **User:** `sa`, **Password:** (пусто)
- Выполнить `SELECT * FROM USERS;` и `SELECT * FROM USER_ROLES;`

---

## Сводная таблица эндпоинтов

| #  | Метод   | URL                               | Доступ  | Описание                   |
|----|---------|-----------------------------------|---------|----------------------------|
| 1  | POST    | `/auth/register`                  | Публично | Регистрация               |
| 2  | POST    | `/auth/login`                     | Публично | Вход                      |
| 3  | POST    | `/auth/refresh`                   | Публично | Обновление токенов        |
| 4  | GET     | `/auth/me`                        | JWT     | Текущий пользователь      |
| 5  | GET     | `/api/cars`                       | JWT     | Список машин              |
| 6  | POST    | `/api/cars`                       | ADMIN   | Создать машину            |
| 7  | PUT     | `/api/cars/{id}`                  | ADMIN   | Полное обновление         |
| 8  | PATCH   | `/api/cars/{id}`                  | ADMIN   | Частичное обновление      |
| 9  | DELETE  | `/api/cars/{id}`                  | ADMIN   | Удалить машину            |
| 10 | GET     | `/users`                          | ADMIN   | Все пользователи          |
| 11 | GET     | `/users/{id}`                     | JWT     | Пользователь по ID        |
| 12 | PUT     | `/users/{id}/personal-data`       | JWT     | Обновить личные данные    |
| 13 | PUT     | `/users/{id}/password`            | JWT     | Сменить пароль            |
| 14 | PUT     | `/users/{id}/birth-date`          | JWT     | Обновить дату рождения    |
| 15 | POST    | `/users/email`                    | JWT     | Установить email          |
| 16 | PATCH   | `/users/{id}/role`                | ADMIN   | Изменить роль             |
| 17 | DELETE  | `/users/{id}`                     | ADMIN   | Удалить пользователя      |
