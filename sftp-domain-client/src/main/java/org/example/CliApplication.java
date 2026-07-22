package org.example;

import com.jcraft.jsch.SftpException;

import java.io.IOException;
import java.util.Map;
import java.util.Scanner;

public class CliApplication {

    private final Scanner scanner;
    private final SftpService sftpService;
    private final DomainService domainService;

    private boolean isRunning;
    private boolean isNotConnected;

    public CliApplication() {
        scanner = new Scanner(System.in);
        sftpService = new SftpService();
        domainService = new DomainService();

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

    private void printMap(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            System.out.println("no addresses");
            return;
        }

        System.out.println("addresses:");
        for (Map.Entry<String, String> entry : map.entrySet()) {
            System.out.println(entry.getKey() + " -> " + entry.getValue());
        }
    }

    private void printPair(String address1, String address2) {
        System.out.println(address1 + " -> " + address2);
    }

    private boolean connectToServer() {
        System.out.println("Enter the host, port, username, and password to connect to the server (press Enter for defaults):");

        System.out.print("host [127.0.0.1]: ");
        String hostInput = scanner.nextLine().trim();
        String host = hostInput.isEmpty() ? "127.0.0.1" : hostInput;

        System.out.print("port [22]: ");
        String portInput = scanner.nextLine().trim();
        int port = portInput.isEmpty() ? 22 : Integer.parseInt(portInput);

        System.out.print("username [user]: ");
        String usernameInput = scanner.nextLine().trim();
        String username = usernameInput.isEmpty() ? "user" : usernameInput;

        System.out.print("password [pass]: ");
        String passwordInput = scanner.nextLine().trim();
        String password = passwordInput.isEmpty() ? "pass" : passwordInput;

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
                try {
                    sftpService.downloadJson();
                    printMap(domainService.getAllMapping());
                } catch (SftpException e) {
                    printError("no such file");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            case 2:
                try {
                    sftpService.downloadJson();
                    System.out.println("write domain: ");
                    String domain = scanner.nextLine().trim();
                    printPair(domain, domainService.getIp(domain));
                } catch (SftpException e) {
                    printError("no such file");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            case 3:
                try {
                    sftpService.downloadJson();
                    System.out.println("write ip: ");
                    String ip = scanner.nextLine().trim();
                    printPair(ip, domainService.getDomain(ip));
                } catch (SftpException e) {
                    printError("no such file");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            case 4:
                System.out.println(4);
                break;
            case 5:
                System.out.println(5);
                break;
            case 6:
                System.out.println(6);
                sftpService.disconnect();
                stop();
                break;
        }
    }

    public void start() {
        while (isNotConnected) {
            try {
                if (connectToServer()) {
                    isNotConnected = false;
                    sftpService.downloadJson();
                }
            } catch (RuntimeException e) {
                printError("failed to connect");
            } catch (SftpException e) {
                printError("not file");
                stop();
            }
        }
        while (isRunning) {
            printMenu();
            System.out.println("Select an item");
            selectItem();
        }
    }
}
