package jm.task.core.jdbc.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

class CustomConnectionPool implements AutoCloseable {
    private static final Logger logger = LogManager.getLogger();
    private static final Lock locker = new ReentrantLock();
    private static CustomConnectionPool instance;
    private final BlockingQueue<ProxyConnection> freeConnection;
    private final Queue<ProxyConnection> givenAwayConnections;
    private static AtomicBoolean isCreated = new AtomicBoolean();
    static final int POOL_SIZE = 4;

    private CustomConnectionPool() {
        freeConnection = new LinkedBlockingDeque<>(POOL_SIZE);
        givenAwayConnections = new LinkedBlockingDeque<>(POOL_SIZE);
        logger.log(Level.INFO, "Try to create connection pool");
        for (int i = 0; i < POOL_SIZE; i++) {
            try {
                Connection connection = ConnectionCreator.getConnection();
                ProxyConnection proxyConnection = new ProxyConnection(connection);
                freeConnection.add(proxyConnection);
            } catch (SQLException e) {
                logger.log(Level.ERROR, "coudn't create connection to data base: " + e.getMessage());
            }
        }
        if (freeConnection.size() == 0) {
            logger.log(Level.FATAL, "connections poll don't created, pool size: " + freeConnection.size());
            throw new RuntimeException("connections poll don't created");
        }
        logger.log(Level.INFO, "Connection pool was created");
    }

    public static CustomConnectionPool getInstance() {
        if (!isCreated.get()) {
            locker.lock();
            if (instance == null) {
                instance = new CustomConnectionPool();
                isCreated.set(true);
            }
            locker.unlock();
        }
        return instance;
    }

    public Connection getConnection() {
        Connection connection = null;
        try {
            connection = freeConnection.take();
            givenAwayConnections.add((ProxyConnection) connection);
        } catch (InterruptedException e) {
            logger.log(Level.ERROR, "InterruptedException in method getConnection " + e.getMessage());
            Thread.currentThread().interrupt();
        }
        return connection;
    }

    public void releaseConnection(Connection connection) {
        if (connection instanceof ProxyConnection && givenAwayConnections.remove(connection)) {
            try {
                freeConnection.put((ProxyConnection) connection);
            } catch (InterruptedException e) {
                logger.log(Level.ERROR, "InterruptedException in method getConnection " + e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
    }

    public void destroyPool() {
        for (int i = 0; i < POOL_SIZE; i++) {
            closeAnyConnection();
        }
        deregisterDrivers();
    }

    private void closeAnyConnection() {
        try {
            freeConnection.take().reallyClose();
        } catch (InterruptedException e) {
            logger.log(Level.ERROR, "InterruptedException in method destroyPool " + e.getMessage());
        } catch (SQLException e) {
            logger.log(Level.ERROR, "SQLException in method destroyPool " + e.getMessage());
        }
    }

    private void deregisterDrivers() {
        DriverManager.getDrivers().asIterator().forEachRemaining(driver -> {
            try {
                DriverManager.deregisterDriver(driver);
            } catch (SQLException e) {
                logger.log(Level.ERROR, "SQLException in method deregisterDrivers " + e.getMessage());
            }
        });
    }

    @Override
    public void close() {
        destroyPool();
    }
}

