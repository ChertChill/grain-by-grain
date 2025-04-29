package api;

import database.DataLoader;
import database.DatabaseConnection;
import transactions.Transaction;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class Main {

    public static void main(String[] args) {
        RestAPI.start();
        System.out.println("API сервер запущен на порту 7070");
        try {
            DatabaseConnection.start();
            DataLoader.loadEverything();
        } catch (SQLException e) {
            System.out.println("Database Connection Error.");
            e.printStackTrace();
        }

    }
}