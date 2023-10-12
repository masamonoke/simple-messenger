# Note
Project is still in development and some things may change
# About
This is an implementation of simple messenger application built with Spring Boot 6 and a little frontend on React
# Usage
For now implemented:
* Authorization and authentication with JWT (session is hold in JWT and these tokens is revoked on logout)
* Users can register and logout
* Users can change their profile (edit email, password, first name, last name), delete account and restore it
* Users can exchange with messages (through websockets, to try feature use frontend in /frontend/react-client). Users can restrict exchange to only with friends.
* Users can add friends and hide friends list, watch others friends
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

Also you can check [Swagger](https://app.swaggerhub.com/apis-docs/MASAMONOKE_1/simple-messenger/1.0.0) to learn about API.

## Authorization and authentication
All authorization and authentication endpoints is excluded from Spring Security
### Register
Adds new user to database and returns access and refresh JWT.
Note that password is not validating (yet).
#### Request
```
POST: localhost:3003/api/v1/auth/register
```

Note that even if you put "role": "Admin", the server will ignore it and create simple user

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
Receives user credentials and returns access and refresh tokens.
Note that if you authenticate after for e.g. register then all previous tokens will be revoked and no longer valid
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
Receives refresh token in Authorization header and returns new valid access token.
Note that is returns refresh token and it is the same that was passed within Authorization header.

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

## User Profile
### Get user by id
To get user profile of user with id 1:
```
GET: localhost:3003/api/v1/profile/user?id=1
```
Note that user profile can be viewed only by owner and by admin

#### Authorization
```javascript
{
    Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTY5Njc3NjQxMywiZXhwIjoxNjk2ODYyODEzfQ.85-nNW0x3L4F-MIjU_iFhrtjvZB8JXTPBXXXo-KnV5A
}
```
#### CURL
```console
curl --location 'localhost:3003/api/v1/profile/user?id=1' \
--header 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTY5NzEwNTU0NiwiZXhwIjoxNjk3MTkxOTQ2fQ.my7Ky_mmak7G3cP7-d1E6k40hc-3JxOgPaxgSN5cAH8'
```

All profile updates can be made only by owner and admin. In code you can change it in ```UserProfileService.isValidUser()``` method.
### Update first name
```
PUT: localhost:3003/api/v1/profile/user/first_name?id=1&first_name=Sas
```
#### CURL
```console
curl --location --request PUT 'localhost:3003/api/v1/profile/user/first_name?id=1&first_name=Sas' \
--header 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTY5NzA4OTQ3NiwiZXhwIjoxNjk3MTc1ODc2fQ.p_o9HRkKWgPqBEVbUGtrg3gG3ykdQOf-jX_3P7GMNqk'
```

### Update last name
```
PUT: localhost:3003/api/v1/profile/user/last_name?id=1&last_name=Sas
```
#### CURL
```console
curl --location --request PUT 'localhost:3003/api/v1/profile/user/last_name?id=1&last_name=Sas' \
--header 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTY5NzA4OTQ3NiwiZXhwIjoxNjk3MTc1ODc2fQ.p_o9HRkKWgPqBEVbUGtrg3gG3ykdQOf-jX_3P7GMNqk'
```

### Update password
Note that is this request password is being sent in request body
```
PUT: localhost:3003/api/v1/profile/user/password
```
```javascript
{
    "id": 1,
    "password": "qwertyuiop"
}
```
#### CURL
```console
curl --location --request PUT 'localhost:3003/api/v1/profile/user/password' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTY5NzA4OTQ3NiwiZXhwIjoxNjk3MTc1ODc2fQ.p_o9HRkKWgPqBEVbUGtrg3gG3ykdQOf-jX_3P7GMNqk' \
--data '{
    "id": 1,
    "password": "qwertyuiop"
}'
```

### Update email
Note that there is no email validation (yet)
```
PUT: localhost:3003/api/v1/profile/user/email?id=1&email=somenew@test.com
```

#### CURL
```console
curl --location --request PUT 'localhost:3003/api/v1/profile/user/email?id=1&email=somenew%40test.com' \
--header 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTY5NzA4OTQ3NiwiZXhwIjoxNjk3MTc1ODc2fQ.p_o9HRkKWgPqBEVbUGtrg3gG3ykdQOf-jX_3P7GMNqk'
```

### Delete account
Note that deletion is not in the literal sense, but deactivation of account which user can restore
Delete user with id = 1:
```
DELETE: localhost:3003/api/v1/profile/user?id=1
```
```console
curl --location --request DELETE 'localhost:3003/api/v1/profile/user?id=1' \
--header 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTY5NzAzNjI0MywiZXhwIjoxNjk3MTIyNjQzfQ.Nc1MuFwhoP8Xn69prbxsAARJjZTqnJZheyNYuUCrxdo'
```

### Restore account
Enabling account. Note that there is no token in header (authorization not possible if account is disabled), so there is only request body with credentials. 
This endpoint is excluded from Spring Security check.
```
PUT: localhost:3003/api/v1/profile/user/restore
```
```javascript
{
    "username": "testuser",
    "email": "testemail@test.com",
    "password": "zxczxvcvn"
}
```
#### CURL
```console
curl --location --request PUT 'localhost:3003/api/v1/profile/user/restore' \
--header 'Content-Type: application/json' \
--data-raw '{
    "username": "testuser",
    "email": "testemail@test.com",
    "password": "zxczxvcvn"
}'
```

### Add a friend
Note that it is not friend request, friend is added to list immedately after successfull request
Adding to friends list user with username = pepuser
```
PUT: localhost:3003/api/v1/profile/user/add_friend?friend=pepuser
```
#### CURL
```console
curl --location --request PUT 'localhost:3003/api/v1/profile/user/add_friend?friend=pepuser' \
--header 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTY5NzEwMTM4MSwiZXhwIjoxNjk3MTg3NzgxfQ.DKppGaIw6wMbc5cx3ubmb-lmLo17i-AGOsFoZBz4Hwc'
```

### Get list of your friends
Note that user of whom friends will be got is taken from token
```
GET: localhost:3003/api/v1/profile/user/friends
```
#### CURL
```console
curl --location 'localhost:3003/api/v1/profile/user/friends' \
--header 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTY5NzEwMTYyMywiZXhwIjoxNjk3MTg4MDIzfQ.Xd7DIg4r842ypYwkELqe1Wk6hYQkpqKOZbjaR6-EgI4'
```

### Get another user's friends
Note that user can hide his friends list and in this case you get 403 response.
Getting friends of user with id = 1:
```
GET: localhost:3003/api/v1/profile/user/friends/1
```

```console
curl --location 'localhost:3003/api/v1/profile/user/friends/1' \
--header 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJwZXB1c2VyIiwiaWF0IjoxNjk3MDk5OTg4LCJleHAiOjE2OTcxODYzODh9.LC8XfLdksR8LgemtSCJWS_3sby-szrfLduADqNoXJYo'
```

### Hide friends list
Hide friends list:
```
PUT: localhost:3003/api/v1/profile/user/hide_friends?hide=true
```
If you want to reveal you friends list then pass hide=false

```console
curl --location --request PUT 'localhost:3003/api/v1/profile/user/hide_friends' \
--header 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTY5NzA5OTk4NCwiZXhwIjoxNjk3MTg2Mzg0fQ.ZhqUM8pCdqYKRri8GHkVd3aciIgGpFcjzTOnkLJOMlw'
```

### Restrict messages
Similiar to previous endpoint. Forbids to receive messages from no one but friends.
```
localhost:3003/api/v1/profile/user/restrict_messages?restrict=true
```

```console
curl --location --request PUT 'localhost:3003/api/v1/profile/user/restrict_messages?restrict=true' \
--header 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTY5NzEwMzYyMywiZXhwIjoxNjk3MTkwMDIzfQ.4xg9rxOOjEEv2pDnXEpfZtks4XXUFSiEqolwz6vygQY'
```

# Messaging
Messages is transfered through websockets. Websockets have their own security configuration and endpoint ```/ws``` through which clients connecting to them is excluded from Spring Security 
and enforced with custom websocket authentication configuration that is similar to global security but works only with sockets.

There is one get request to get all message sent by requesting user and filtered by receiver:
```
GET: localhost:3003/api/v1/message?receiver_username=pepuser
```
#### CURL
```console
curl --location 'localhost:3003/api/v1/message?receiver_username=pepuser' \
--header 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJwZXB1c2VyIiwiaWF0IjoxNjk3MDkwMzc5LCJleHAiOjE2OTcxNzY3Nzl9.PVQBy6DIRS4n97WzXJR263KSeHgtOLowvFHbCJV3CC4'
```
To send message to public chatroom in your socket client configure STOMP client like:
```javascript
stompClient.send("/app/message", {}, JSON.stringify(chatMessage));
```
Or in case with private message:
```javascript
stompClient.send("/app/private_message", {}, JSON.stringify(chatMessage));
```
All send endpoints is prefixed with /app.

To subscribe to sockets:
```javascript
let Sock = new SockJS('http://localhost:3003/ws')
stompClient = over(Sock);
stompClient.connect({"Authorization": userData.token}, onConnected, onError);
...
stompClient.subscribe('/topic/public', onMessageReceived);
stompClient.subscribe('/user/' + userData.username + '/private', onPrivateMessage);
```

User logins to chat with own token:
<img width="563" alt="image" src="https://github.com/masamonoke/simple-messenger/assets/68110536/14999d7f-7d14-46f9-9b71-8f0d58d2bc8d"><br>

Chatroom with 2 users connected to websockets:
<img width="1384" alt="image" src="https://github.com/masamonoke/simple-messenger/assets/68110536/d597cae3-e81a-43f7-92bf-c4ec15de5d6a"><br>

And some messages:
<img width="1365" alt="image" src="https://github.com/masamonoke/simple-messenger/assets/68110536/996efdad-963c-4298-9113-1c4f4e656de7"><br>

These messages flow through websockets and being saved to database:
<img width="849" alt="image" src="https://github.com/masamonoke/simple-messenger/assets/68110536/ea6701bf-8d75-4f91-9aec-b769bab4bce2">

Frontend is opened on port 3000.
