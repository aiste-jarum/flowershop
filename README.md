# AiGeles

![Screenshot_2021-04-29-12-56-45](https://user-images.githubusercontent.com/42966588/119275373-486b5800-bc1d-11eb-837f-2b46abb88a2d.png)

## How to run project

1. Install PostgreSQL 13.2

   1. Download and install PostgreSQL

   1. Create user `user1` with password `45685213`

   1. Create database `gelytes` with owner `user1`, all permissions

1. Install Node.js v14 with npm v6
1. Use IntelliJ to run geles-backend project (Maven + Spring Boot)

   - It is possible to run `./mvnw spring-boot:run` if on Linux or with UTF-8 encoding configured on Windows

1. Use any terminal to run geles-frontend

   1. Run `npm i` to install all dependencies
   1. Run `npm start` to launch the server

## Functionality

### General actions

- View all flowers
- View detailed flower info
- Search, sort and filter flower listings

### User actions

- Add flowers to cart
- Save carts for future
- Create order from cart
- Mark flowers as favorite
- View favorite flowers

### Admin actions

- Add/update/remove flower listings
- Favorite flower statistics

## Sample users

| Username | Password    | Is admin |
| -------- | ----------- | -------- |
| emilis   | password123 | false    |
| justas   | 12345678    | false    |
| titas    | 12345678    | false    |
| simonas  | 12345678    | false    |
| admin    | 12345678    | true     |
