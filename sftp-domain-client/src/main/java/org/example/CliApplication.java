package org.example;

import java.util.Scanner;

public class CliApplication {

    private final Scanner scanner;
    private final SftpService sftpService;

    private boolean isRunning;
    private boolean isNotConnected;

    public CliApplication() {
        scanner = new Scanner(System.in);
        sftpService = new SftpService();

        isRunning = true;
        isNotConnected = true;
    }

    private void printMenu(){
        System.out.println("1. Получение списка пар домен – адрес из файла");
        System.out.println("2. Получение IP-адреса по доменному имени, в случае отсутствия такового вывести сообщение об этом");
        System.out.println("3. Получение доменного имени по IP-адресу, в случае отсутствия такового вывести сообщение об этом");
        System.out.println("4. Добавление новой пары домен – адрес в файл");
        System.out.println("5. Добавление новой пары домен – адрес в файл");
        System.out.println("6. Завершение работы.");
    }

    private void printError(String err) {
        System.err.println("Error: " + err);
        System.out.println();
    }

    private boolean connectToServer() {
        System.out.println("Enter the host, port, username, and password to connect to the server");
        System.out.print("host: ");
        String host = scanner.nextLine();
        System.out.print("port: ");
        int port = Integer.parseInt(scanner.nextLine());
        System.out.print("username: ");
        String username = scanner.nextLine();
        System.out.print("password: ");
        String password = scanner.nextLine();
        System.out.println();
        return sftpService.connect(host, port, username, password);
    }

    private void stop() {
        sftpService.disconnect();
        scanner.close();
        isRunning = false;
    }

    private void selectItem() {
        int item = Integer.parseInt(scanner.nextLine());
        switch (item) {
            case 1:
                System.out.println(1);
                break;
            case 2:
                System.out.println(2);
                break;
            case 3:
                System.out.println(3);
                break;
            case 4:
                System.out.println(4);
                break;
            case 5:
                System.out.println(5);
                break;
            case 6:
                System.out.println(6);
                stop();
                break;
        }
    }

    public void start() {
        while (isNotConnected) {
            try {
                if (connectToServer()) {
                    isNotConnected = false;
                }
            } catch (RuntimeException e) {
                printError("failed to connect");
            }
        }
        while (isRunning) {
            printMenu();
            System.out.println("Select an item");
            selectItem();
        }
    }
}
