# Nexign Bootcamp
Биллинговый сервис тарификации клиентов.

### Используемый стек:
- Java (17)
- Spring (Boot, Data Jpa, Security)
- Apache Kafka
- PostgreSQL
- Docker
- Swagger
- Liquibase
- H2 database

##
### Описание проекта:
Приложение состоит из 4 микросервисов: **CDR, BRT, HRS, CRM**. Взаимодействие между 
ними реализовано с помощью Apache Kafka. Общие DTO вынесены в отдельный
модуль - common.

### 1. CDR сервис
Генерирует рандомные записи о звонках абонентов, эмулируя действия реальных клиентов и работу коммутатора.
Создает данные как о звонках клиентов оператора «Ромашка», так и о звонках клиентов других операторов. 

Каждые 10 записей сохраняет в .txt файл, добавляет данные о совершенных транзакциях в локальную базу данных H2 и отправляет CDR файл в BRT сервис (через Kafka).

> Для увеличения количества генерируемых звонков необходимо уменьшать параметры MinUnixGap/MaxUnixGap (промежуток между звонками в секундах) в конфигурации
> docker-compose.yml

### 2. BRT сервис
Содержит базу данных об абонентах оператора «Ромашка» (номер телефона, тариф и баланс).
Получает данные о совершенных звонках из CDR сервиса. Авторизует абонентов оператора «Ромашка», отсеивая записи с данными клиентов других операторов.

Наполняет данные о звонках дополнительной информацией и передает их в HRS сервис для тарификации (через Kafka). После расчета в HRS производит списание 
с баланса абонентов на основе полученных данных. 

Дополнительно, каждый месяц автоматически пополняет балансы всех клиентов и
изменяет тариф у случайного количества клиентов (от 1 до 3), эмулируя действия реальных пользователей.

### 3. HRS сервис
Получает расширенные данные о звонках из BRT сервиса, рассчитывает стоимость звонка на основе тарифа абонента (для помесячного тарифа дополнительно хранит в кэше 
количество использованных минут в месяце по каждому клиенту). После расчета передает обратно в BRT сервис данные для списания с баланса абонента (через Kafka).

Каждый месяц производит расчет абонентской платы для всех клиентов с помесячным тарифом и передает в BRT сервис данные для списания с баланса абонента. 
Месяц меняется с приходом данных о звонке любого абонента, датируемых следующим месяцем (по сравнению с месяцем в сервисе). 

###
> Каждый сервис дополнительно логирует обрабатываемые данные для проверки.
###

### 4. CRM сервис
Представляет из себя API для менеджера и абонента с различным набором методов для внешнего взаимодействия с системой. Авторизация реализована с помощью JWT-токена, система ролей
реализована с помощью Spring Security и содержит два типа ролей: USER и ADMIN. Поэтому, необходимо сначала пройти авторизацию и получить свой
JWT-токен (дополнительно содержит срок действия и роль), после чего необходимо с вызовом каждого метода передавать его в header запроса. Входные данные валидируются.

Принял решение вынести данный сервис, как отдельный микросервис - для возможности дальнейшего раздельного масштабирования обоих сервисов: CRM и BRT.

###
> Документация OpenAPI доступна через Swagger UI: [http://localhost:8083/swagger-ui/index.html](http://localhost:8083/swagger-ui/index.html).
###

#### Данные для авторизации в CRM:
| Username   | Password | Role | Methods                               |
|------------|----------|------|---------------------------------------|
| 79079765785| password | USER | pay|
| admin      | admin    | ADMIN| save, change-tariff|

###
> [!TIP]
> Просмотреть данные из Kafka topics можно через Kafdrop: [http://localhost:9000/](http://localhost:9000/).
##

### Данные для авторизации в БД:
1. H2
    - jdbc:h2:mem:localdb
    - username: sa
    - password: password
  
> UI для просмотра данных: [http://localhost:8080/h2-console](http://localhost:8080/h2-console).

2. PostgreSQL
    - username: postgres
    - password: postgres
##
### Запуск приложения:
Необходимо сперва перейти в корневую папку проекта (где лежит docker-compose.yml), в терминале написать команду:

1. Для запуска приложения:

```
docker-compose up
```
2. Для остановки приложения:

```
docker-compose down
```

> При ошибке проверить доступность всех необходимых портов.