package com.laioffer.jupiter.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class MySQLTableCreator {
    // Run this as a Java application to reset the database.
    public static void main(String[] args) {
        try {

            // Step 1 Connect to MySQL.
            System.out.println("Connecting to " + com.laioffer.jupiter.db.MySQLDBUtil.getMySQLAddress());
            //jdbc反射机制来创建class，通过一个string
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            Connection conn = DriverManager.getConnection(com.laioffer.jupiter.db.MySQLDBUtil.getMySQLAddress());

            if (conn == null) {
                System.out.println("Driver manager get connection failed");
                return;
            }

            // Step 2 Drop tables in case they exist.
            Statement statement = conn.createStatement();
            //数据库的关键词一般用大写
            String sql = "DROP TABLE IF EXISTS favorite_records";
            statement.executeUpdate(sql);

            sql = "DROP TABLE IF EXISTS items";
            statement.executeUpdate(sql);

            sql = "DROP TABLE IF EXISTS users";
            statement.executeUpdate(sql);

            // Step 3 Create new tables.（先画出ER图）
            //table的一些constrain，NOT NULL就是插入数据的时候必须有，primary key不能重复插入
            //在console里面进行sql操作
            sql = "CREATE TABLE items ("
                    + "id VARCHAR(255) NOT NULL,"
                    + "title VARCHAR(255),"
                    + "url VARCHAR(255),"
                    + "thumbnail_url VARCHAR(255),"
                    + "broadcaster_name VARCHAR(255),"
                    + "game_id VARCHAR(255),"
                    + "type VARCHAR(255) NOT NULL,"
                    + "PRIMARY KEY (id)" //定义主键，可以多个column作为联合主键
                    + ")";
            //执行没有返回值得sql
            statement.executeUpdate(sql);

            sql = "CREATE TABLE users ("
                    + "id VARCHAR(255) NOT NULL,"
                    + "password VARCHAR(255) NOT NULL,"
                    + "first_name VARCHAR(255),"
                    + "last_name VARCHAR(255),"
                    + "PRIMARY KEY (id)"
                    + ")";
            statement.executeUpdate(sql);
            //user和item和record表都是1：N的关系，record表里有多个item，多个user，2个单独都不能作为主键
            //2个加一起形成联合主键
            sql = "CREATE TABLE favorite_records ("
                    + "user_id VARCHAR(255) NOT NULL,"
                    + "item_id VARCHAR(255) NOT NULL,"
                    + "last_favor_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," //db自动fill
                    + "PRIMARY KEY (user_id, item_id),"
                    + "FOREIGN KEY (user_id) REFERENCES users(id),"
                    + "FOREIGN KEY (item_id) REFERENCES items(id)"
                    + ")";
            statement.executeUpdate(sql);

            // Step 4: insert fake user 1111/3229c1097c00d497a0fd282d586be050.
            //按照table scheme定义的顺序插入数据  密码用helper func转成hash值 MD5
            sql = "INSERT INTO users VALUES('1111', '3229c1097c00d497a0fd282d586be050', 'John', 'Smith')";
            statement.executeUpdate(sql);

            conn.close();
            System.out.println("Import done successfully");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}