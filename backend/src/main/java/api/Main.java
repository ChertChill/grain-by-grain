package api;

import database.DatabaseConnection;

import java.sql.SQLException;


public class Main {

    public static void main(String[] args) {
        RestAPI.start();
        System.out.println("API сервер запущен на порту 7070");
        try {
            DatabaseConnection.start();
        } catch (SQLException e) {
            System.out.println("Unable to connect to database.");
            e.printStackTrace();
        }

    }
}