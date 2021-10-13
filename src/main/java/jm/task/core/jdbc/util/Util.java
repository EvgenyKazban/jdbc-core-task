package jm.task.core.jdbc.util;

import java.sql.Connection;

public class Util {
    private static Util instance;

    private Util() {

    }

    public static Util getInstance() {
        if(instance == null) {
            instance = new Util();
        }
        return instance;
    }

    public Connection getConnection() {
        return CustomConnectionPool.getInstance().getConnection();
    }
}
