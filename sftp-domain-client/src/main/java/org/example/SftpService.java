package org.example;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SftpService {
    private Session session;
    private ChannelSftp channelSftp;

    public boolean connect(String host, Integer port, String user, String password) {
        try {
            JSch jsch = new JSch();
            session = jsch.getSession(user, host, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();
            return true;
        } catch (JSchException e) {
            disconnect();
            throw new RuntimeException(e);
        }
    }

    public void disconnect() {
        session.disconnect();
        channelSftp.disconnect();
    }
}
