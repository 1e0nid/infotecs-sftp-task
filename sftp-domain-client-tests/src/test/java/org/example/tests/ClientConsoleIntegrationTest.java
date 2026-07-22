package org.example.tests;

import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertTrue;

public class ClientConsoleIntegrationTest {

    private static final String CLIENT_JAR = System.getProperty(
            "client.jar.path", "../sftp-domain-client/target/sftp-domain-client.jar");

    private static final String HOST = System.getProperty("sftp.test.host", "127.0.0.1");
    private static final String PORT = System.getProperty("sftp.test.port", "2222");
    private static final String USER = System.getProperty("sftp.test.user", "user");
    private static final String PASSWORD = System.getProperty("sftp.test.password", "pass");

    @Test(groups = "integration")
    public void listCommandShowsPairs() throws IOException, InterruptedException {
        String output = runClient("1", "6");
        assertTrue(output.contains("->") || output.contains("список пар пуст"),
                "Ожидался вывод списка пар или сообщение о пустом списке:\n" + output);
    }

    @Test(groups = "integration")
    public void addThenGetIpReturnsStoredValue() throws IOException, InterruptedException {
        String domain = "integration-test-" + System.currentTimeMillis() + ".domain";
        String ip = "10.20.30.40";

        String output = runClient(
                "4", domain, ip,
                "2", domain,
                "5", domain,
                "6");

        assertTrue(output.contains(domain + " -> " + ip),
                "Ожидался только что добавленный адрес в выводе:\n" + output);
    }

    @Test(groups = "integration")
    public void gettingIpForUnknownDomainReportsNotFound() throws IOException, InterruptedException {
        String output = runClient("2", "definitely-does-not-exist.domain", "6");

        assertTrue(output.contains("не найден"), "Ожидалось сообщение об отсутствии домена:\n" + output);
    }

    @Test(groups = "integration")
    public void addingInvalidIpIsRejected() throws IOException, InterruptedException {
        String output = runClient("4", "bad-ip-test.domain", "999.999.999.999", "6");

        assertTrue(output.contains("Ошибка"), "Ожидалось сообщение об ошибке валидации IP:\n" + output);
    }

    @Test(groups = "integration")
    public void addingDuplicateDomainIsRejected() throws IOException, InterruptedException {
        String domain = "dup-test-" + System.currentTimeMillis() + ".domain";

        String output = runClient(
                "4", domain, "10.10.10.10",
                "4", domain, "10.10.10.11",
                "6");

        assertTrue(output.contains("Ошибка"), "Ожидалось сообщение об ошибке дублирования домена:\n" + output);
    }

    private String runClient(String... menuInputs) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("java", "-Dfile.encoding=UTF-8", "-jar", CLIENT_JAR);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (PrintWriter in = new PrintWriter(
                new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8), true);
             BufferedReader out = new BufferedReader(
                     new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {

            in.println(HOST);
            in.println(PORT);
            in.println(USER);
            in.println(PASSWORD);

            for (String line : menuInputs) {
                in.println(line);
            }

            StringBuilder output = new StringBuilder();
            String line;
            while ((line = out.readLine()) != null) {
                output.append(line).append('\n');
            }

            process.waitFor(15, TimeUnit.SECONDS);
            return output.toString();
        } finally {
            if (process.isAlive()) {
                process.destroyForcibly();
            }
        }
    }
}
