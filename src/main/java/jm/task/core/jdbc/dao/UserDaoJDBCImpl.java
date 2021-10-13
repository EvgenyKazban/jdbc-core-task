package jm.task.core.jdbc.dao;

import jm.task.core.jdbc.model.User;
import jm.task.core.jdbc.util.Util;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDaoJDBCImpl implements UserDao {

    public UserDaoJDBCImpl() {
    }

    public void createUsersTable()  {
        try {
            String usersTableQuery = "CREATE TABLE users " +
                    "(id INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY, name TEXT, lastName TEXT, age INTEGER)";
            executeUpdate(usersTableQuery);
        } catch (SQLException exc) {
            exc.printStackTrace();
        }
    }

    public void dropUsersTable() {
        try {
            String usersTableQuery = "DROP TABLE users";
            executeUpdate(usersTableQuery);
        } catch (SQLException exc) {
            exc.printStackTrace();
        }
    }

    private int executeUpdate(String query) throws SQLException {
        try (Connection connection = Util.getInstance().getConnection()) {
            Statement statement = connection.createStatement();
            // Для Update, Delete
            int result = statement.executeUpdate(query);
            return result;
        }
    }

    public void saveUser(String name, String lastName, byte age) {
        try (Connection connection = Util.getInstance().getConnection()){
            String sql = "INSERT INTO users (name, lastName, age) VALUES (?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, lastName);
            preparedStatement.setByte(3, age);
            preparedStatement.executeUpdate();
        } catch (SQLException exc) {
            exc.printStackTrace();
        }
    }


    public void removeUserById(long id) {
        try {
            String usersTableQuery = "DELETE FROM users WHERE id = " + id;
            executeUpdate(usersTableQuery);
        } catch (SQLException exc) {
            exc.printStackTrace();
        }
    }

    public List<User> getAllUsers() {
        ArrayList<User> users = null;
        try (Connection connection = Util.getInstance().getConnection()){
            Statement stmt = connection.createStatement();
            String usersTableQuery = "SELECT * FROM users";
            ResultSet rs = stmt.executeQuery(usersTableQuery);

            users = new ArrayList<>();

            while(rs.next()){
                User user = new User(rs.getString("name"), rs.getString("lastName"), rs.getByte("age"));
                user.setId(rs.getLong("id"));
                users.add(user);
            }
        } catch (SQLException exc) {
            exc.printStackTrace();
        }

        return users;
    }

    public void cleanUsersTable() {
        try {
            String usersTableQuery = "TRUNCATE TABLE users";
            executeUpdate(usersTableQuery);
        } catch (SQLException exc) {
            exc.printStackTrace();
        }
    }
}
