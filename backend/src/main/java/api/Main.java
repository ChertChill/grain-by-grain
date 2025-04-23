package api;

import authorization.AuthorizationHandler;
import org.mindrot.jbcrypt.BCrypt;

public class Main {
    public static void main(String[] args) {
        RestAPI.start();
        System.out.println("API сервер запущен на порту 7070");
    }
}