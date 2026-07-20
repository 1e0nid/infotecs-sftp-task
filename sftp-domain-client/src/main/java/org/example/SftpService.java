package org.example;

import com.jcraft.jsch.*;

public class SftpService {
    private Session session;
    private ChannelSftp channelSftp;

    private final String pathServerDir = "upload/test.json";
    private final String pathDownloadDir = "./sftp-domain-client/download/";

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

    public void downloadJson() throws SftpException {
        channelSftp.get(pathServerDir, pathDownloadDir);
    }

    public void disconnect() {
        session.disconnect();
        channelSftp.disconnect();
    }
}
