package com.impact.AutoMagazin.database;

public class UserStore {
    spring:
        application:
            name:lessons

    datasourse:
    url: jbc:postgresql://localhost:5432/test
    username: postgres
    password: 1234

    jpa:
        hibernate:
            ddl-auto:update
        show-sql:true

    sql:
        init:
            mode: always

}
