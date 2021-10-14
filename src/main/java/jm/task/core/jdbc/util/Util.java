package jm.task.core.jdbc.util;

import java.sql.Connection;

public class Util {
    private static volatile Util instance;

    private Util() {

    }

    public static Util getInstance() {
        Util localInstance = instance;
        if (localInstance == null) {
            synchronized (Util.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new Util();
                }
            }
        }
        return localInstance;
    }

    public Connection getConnection() {
        return CustomConnectionPool.getInstance().getConnection();
    }
}
