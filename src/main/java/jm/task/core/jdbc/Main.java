package jm.task.core.jdbc;

import jm.task.core.jdbc.model.User;
import jm.task.core.jdbc.service.UserServiceImpl;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        UserServiceImpl userService = new UserServiceImpl();
        userService.createUsersTable();
        userService.saveUser("Evgeniy", "Smith", (byte) 25);
        userService.saveUser("Alice", "Moore", (byte) 15);
        userService.saveUser("Maria", "Rossi", (byte) 20);
        userService.saveUser("John", "Fowler", (byte) 30);

        List<User> users = userService.getAllUsers();
        for(User user : users) {
            System.out.println(user);
        }

        userService.cleanUsersTable();
        userService.dropUsersTable();
    }
}
