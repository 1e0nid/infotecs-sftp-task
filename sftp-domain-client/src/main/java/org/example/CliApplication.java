package org.example;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import java.io.Console;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

public class CliApplication {

    private static final int OPTION_LIST = 1;
    private static final int OPTION_IP_BY_DOMAIN = 2;
    private static final int OPTION_DOMAIN_BY_IP = 3;
    private static final int OPTION_ADD = 4;
    private static final int OPTION_REMOVE = 5;
    private static final int OPTION_EXIT = 6;

    private final Scanner scanner;
    private final SftpService sftpService;
    private final DomainService domainService;

    private boolean running;

    public CliApplication() {
        this.scanner = new Scanner(System.in);
        this.sftpService = new SftpService();
        this.domainService = new DomainService();
        this.running = true;
    }

    public void start() {
        if (!connectLoop()) {
            return;
        }

        while (running) {
            printMenu();
            handleSelection();
        }
    }

    private boolean connectLoop() {
        while (true) {
            ConnectionParams params = readConnectionParams();

            try {
                sftpService.connect(params.host, params.port, params.user, params.password);
                sftpService.downloadJson();
                System.out.println("Успешное подключение к серверу.\n");
                return true;
            } catch (JSchException e) {
                printError("не удалось подключиться к серверу: " + e.getMessage());
                sftpService.disconnect();
            } catch (SftpException e) {
                printError("подключение установлено, но не удалось получить файл на сервере: " + e.getMessage());
                sftpService.disconnect();
            } catch (IOException e) {
                printError("не удалось сохранить файл локально: " + e.getMessage());
                sftpService.disconnect();
            }

            if (!askRetry()) {
                return false;
            }
        }
    }

    private boolean askRetry() {
        System.out.print("Повторить попытку подключения? (y/n): ");
        String answer = scanner.nextLine().trim().toLowerCase();
        return answer.equals("y") || answer.equals("yes") || answer.equals("да");
    }

    private ConnectionParams readConnectionParams() {
        System.out.println("Введите параметры подключения к SFTP-серверу:");

        String host;
        while (true) {
            System.out.print("Адрес хоста: ");
            host = scanner.nextLine().trim();
            if (!host.isEmpty()) {
                break;
            }
            printError("адрес хоста не может быть пустым");
        }

        int port;
        while (true) {
            System.out.print("Порт: ");
            String portInput = scanner.nextLine().trim();
            try {
                port = Integer.parseInt(portInput);
                if (port < 1 || port > 65535) {
                    printError("порт должен быть в диапазоне 1-65535");
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                printError("порт должен быть числом");
            }
        }

        System.out.print("Логин: ");
        String user = scanner.nextLine().trim();

        String password = readPassword();

        System.out.println();
        return new ConnectionParams(host, port, user, password);
    }

    private String readPassword() {
        Console console = System.console();
        if (console != null) {
            char[] pwd = console.readPassword("Пароль: ");
            return pwd == null ? "" : new String(pwd);
        }
        // Консоль недоступна (например, запуск из IDE) — вводим как обычно
        System.out.print("Пароль: ");
        return scanner.nextLine();
    }

    private void printMenu() {
        System.out.println("Выберите действие:");
        System.out.println(OPTION_LIST + ". Получение списка пар \"домен - адрес\" из файла");
        System.out.println(OPTION_IP_BY_DOMAIN + ". Получение IP-адреса по доменному имени");
        System.out.println(OPTION_DOMAIN_BY_IP + ". Получение доменного имени по IP-адресу");
        System.out.println(OPTION_ADD + ". Добавление новой пары \"домен - адрес\"");
        System.out.println(OPTION_REMOVE + ". Удаление пары \"домен - адрес\" по домену или IP");
        System.out.println(OPTION_EXIT + ". Завершение работы");
        System.out.print("> ");
    }

    private void handleSelection() {
        int item;
        try {
            item = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            printError("введите число от 1 до 6");
            return;
        }

        try {
            switch (item) {
                case OPTION_LIST:
                    handleList();
                    break;
                case OPTION_IP_BY_DOMAIN:
                    handleIpByDomain();
                    break;
                case OPTION_DOMAIN_BY_IP:
                    handleDomainByIp();
                    break;
                case OPTION_ADD:
                    handleAdd();
                    break;
                case OPTION_REMOVE:
                    handleRemove();
                    break;
                case OPTION_EXIT:
                    stop();
                    break;
                default:
                    printError("нет пункта меню с номером " + item);
            }
        } catch (SftpException e) {
            printError("файл не найден на сервере: " + e.getMessage());
        } catch (IOException e) {
            printError("ошибка ввода-вывода: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            printError(e.getMessage());
        }
    }

    private void handleList() throws SftpException, IOException {
        sftpService.downloadJson();
        printMap(domainService.getAllMapping());
    }

    private void handleIpByDomain() throws SftpException, IOException {
        sftpService.downloadJson();
        System.out.print("Введите домен: ");
        String domain = scanner.nextLine().trim();

        Optional<String> ip = domainService.getIp(domain);
        if (ip.isPresent()) {
            printPair(domain, ip.get());
        } else {
            System.out.println("для домена \"" + domain + "\" адрес не найден");
        }
    }

    private void handleDomainByIp() throws SftpException, IOException {
        sftpService.downloadJson();
        System.out.print("Введите IP-адрес: ");
        String ip = scanner.nextLine().trim();

        Optional<String> domain = domainService.getDomain(ip);
        if (domain.isPresent()) {
            printPair(ip, domain.get());
        } else {
            System.out.println("для адреса \"" + ip + "\" домен не найден");
        }
    }

    private void handleAdd() throws SftpException, IOException {
        sftpService.downloadJson();
        System.out.print("Введите домен: ");
        String domain = scanner.nextLine().trim();
        System.out.print("Введите IP-адрес: ");
        String ip = scanner.nextLine().trim();

        domainService.addPair(domain, ip);
        sftpService.uploadFile();
        System.out.println("Пара добавлена: " + domain + " -> " + ip);
    }

    private void handleRemove() throws SftpException, IOException {
        sftpService.downloadJson();
        System.out.print("Введите домен или IP-адрес: ");
        String identifier = scanner.nextLine().trim();

        if (domainService.removePair(identifier)) {
            sftpService.uploadFile();
            System.out.println("Пара с \"" + identifier + "\" удалена");
        } else {
            System.out.println("пара с \"" + identifier + "\" не найдена");
        }
    }

    private void printMap(Map<String, String> map) {
        if (map.isEmpty()) {
            System.out.println("список пар пуст");
            return;
        }
        for (Map.Entry<String, String> entry : map.entrySet()) {
            printPair(entry.getKey(), entry.getValue());
        }
    }

    private void printPair(String domain, String ip) {
        System.out.println(domain + " -> " + ip);
    }

    private void printError(String message) {
        System.out.println("Ошибка: " + message);
    }

    private void stop() {
        running = false;
        sftpService.disconnect();
        scanner.close();
        System.out.println("Работа завершена.");
    }

    private static final class ConnectionParams {
        final String host;
        final int port;
        final String user;
        final String password;

        ConnectionParams(String host, int port, String user, String password) {
            this.host = host;
            this.port = port;
            this.user = user;
            this.password = password;
        }
    }
}