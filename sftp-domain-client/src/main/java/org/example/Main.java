package org.example;

public class Main {
    public static void main(String[] args) {
        try {
            CliApplication cliApplication = new CliApplication();
            cliApplication.start();
        } catch (Throwable t) {
            System.err.println("Критическая ошибка: " + t.getMessage());
            System.exit(1);
        }
    }
}