package org.example;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SftpService {
    private String serverHost;
    private String serverPort;
    private String username;
    private String password;

    public boolean connect(String host, Integer port, String user, String password) {
        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
        } catch (JSchException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
}
