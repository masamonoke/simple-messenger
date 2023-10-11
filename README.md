# Note
Project is still in development and some things may change
# About
This is an implementation of simple messenger application built with Spring Boot and a little frontend on React
# Usage
For now implemented:
* Authorization and authentication with JWT
* Users can register and logout
* Users can change their profile (edit email, password, first name, last name), delete account and restore it
# Requirements
* Docker
* Maven
* npm (if you want to try websocket message exchanging)
# Build
Pay attention that complete build will take these ports:
* 8080
* 8025
* 5434
* 3003
* 3000
1. Before all else you need to aggregate all needed docker containers. To do this from root run this command in shell:
```console
docker-compose up
```
Or you can use interactive UI from IDEA for e.g. and open docker-compose.yml file then click on topmost green triangle:

<img width="452" alt="image" src="https://github.com/masamonoke/simple-messenger/assets/68110536/f2d01138-3b64-4d26-b4bd-fee8cfd33ae9"><br>

2. After that you need to build Spring Boot application. You can run shell command:
```console
mvn spring-boot:run
```
Or run as usual in your IDE.

3. To use websokets messaging features you need frontend application. To build it move to:
```console
cd frontend/react-client
```
Build node modules:
```console
npm install
```
And run app:
```console
npm run start
```

# API
## Authorization and authentication
### Register
Adds new user to database and returns access and refresh JWT
#### Request
```
POST: localhost:3003/api/v1/auth/register
```
#### Payload example
```javascript
{
    "username": "testuser",
    "email": "testemail@test.com",
    "password": "1234"
}
```
#### Response example
```javascript
{
    "access_token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTY5NzAzNjI0MywiZXhwIjoxNjk3MTIyNjQzfQ.Nc1MuFwhoP8Xn69prbxsAARJjZTqnJZheyNYuUCrxdo",
    "refresh_token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTY5NzAzNjI0MywiZXhwIjoxNjk3NjQxMDQzfQ.6BqkCe7UMRXQK37uoxkP72L3uWyDzujz9xlKjDKY0XY"
}
```
#### CURL
```console
curl --location 'localhost:3003/api/v1/auth/register' \
--header 'Content-Type: application/json' \
--data-raw '{
    "username": "testuser",
    "email": "testemail@test.com",
    "password": "1234"
}'
```
### Authenication
Receives user credentials and returns access and refresh tokens
```
POST: localhost:3003/api/v1/auth/authenticate
```
#### Payload example
```javascript
{
    "username": "testuser",
    "password": "1234"
}
```
#### Response example
```javascript
{
    "access_token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTY5NzAzNjI0MywiZXhwIjoxNjk3MTIyNjQzfQ.Nc1MuFwhoP8Xn69prbxsAARJjZTqnJZheyNYuUCrxdo",
    "refresh_token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTY5NzAzNjI0MywiZXhwIjoxNjk3NjQxMDQzfQ.6BqkCe7UMRXQK37uoxkP72L3uWyDzujz9xlKjDKY0XY"
}
```
#### CURL
```console
curl --location 'localhost:3003/api/v1/auth/authenticate' \
--header 'Content-Type: application/json' \
--data '{
    "username": "testuser",
    "password": "1234"
}'
```

### Refresh
Receives refresh token in Authorization header and returns new valid access token

```
POST: localhost:3003/api/v1/auth/refresh
```
#### Header example 
```javascript
{
  Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTY5Njc3NjQxMywiZXhwIjoxNjk2ODYyODEzfQ.85-nNW0x3L4F-MIjU_iFhrtjvZB8JXTPBXXXo-KnV5A
}
```
#### Response example
```javascript
{
    "access_token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTY5NzAzNjI0MywiZXhwIjoxNjk3MTIyNjQzfQ.Nc1MuFwhoP8Xn69prbxsAARJjZTqnJZheyNYuUCrxdo",
    "refresh_token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTY5NzAzNjI0MywiZXhwIjoxNjk3NjQxMDQzfQ.6BqkCe7UMRXQK37uoxkP72L3uWyDzujz9xlKjDKY0XY"
}
```
#### CURL
```console
curl --location --request POST 'localhost:3003/api/v1/auth/refresh' \
--header 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTY5NzA0MzExMCwiZXhwIjoxNjk3NjQ3OTEwfQ.DSQH1iMxFwCiWbldt4u3xt1JrDPAaemdecS_aWVztTU'
```

### Logout
Receives access token in Authorization header and invalidates all user active tokens

```
POST: localhost:3003/api/v1/auth/logout
```
#### Header example 
```javascript
{
  Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTY5Njc3NjQxMywiZXhwIjoxNjk2ODYyODEzfQ.85-nNW0x3L4F-MIjU_iFhrtjvZB8JXTPBXXXo-KnV5A
}
```
#### CURL
```console
curl --location --request POST 'localhost:3003/api/v1/auth/logout' \
--header 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTY5Njc3NjQyOSwiZXhwIjoxNjk2ODYyODI5fQ.c2hW5Wc4QERc95AlCclm8At5F5hcaaHQaNyX36Yz_MA'
```

### Email confirmation
This request is generated automatically and user makes it when follow link in specified email. If you want to make this request then open in your browser 0.0.0.0:8080 and you will see
something like this after you register:

<img width="1440" alt="image" src="https://github.com/masamonoke/simple-messenger/assets/68110536/25fbe00d-2e4a-48db-ae7b-c1f5e3661910"><br>

The link in email is request to confirm with confirmation token included:

<img width="1068" alt="image" src="https://github.com/masamonoke/simple-messenger/assets/68110536/b9bf0288-f623-4ec6-9123-f08695dd19bb"><br>


```
GET: localhost:3003/api/v1/auth/confirm?token=66e06434-bf17-475e-ab24-b60887c883e8
```

