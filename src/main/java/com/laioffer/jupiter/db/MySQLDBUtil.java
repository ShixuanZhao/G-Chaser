package com.laioffer.jupiter.db;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class MySQLDBUtil {
    private static final String INSTANCE = "laiproject-instance.cg1lj5qtbhdy.us-east-2.rds.amazonaws.com";//endpoint addr
    private static final String PORT_NUM = "3306";
    private static final String DB_NAME = "jupiter_db";
    public static String getMySQLAddress() throws IOException {
        //专门读取propertity
        Properties prop = new Properties();
        String propFileName = "config.properties";
        //在当前project里面读，相当于读取相对路径
        InputStream inputStream = MySQLDBUtil.class.getClassLoader().getResourceAsStream(propFileName);
        prop.load(inputStream);

        String username = prop.getProperty("user");
        String password = prop.getProperty("password");
        //协议+域名端口（INSTANCE, PORT_NUM, DB_NAME）
        return String.format("jdbc:mysql://%s:%s/%s?user=%s&password=%s&autoReconnect=true&serverTimezone=UTC&createDatabaseIfNotExist=true",
                INSTANCE, PORT_NUM, DB_NAME, username, password);
    }

}