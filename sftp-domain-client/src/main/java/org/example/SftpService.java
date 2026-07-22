package org.example;

import com.jcraft.jsch.*;

public class SftpService {
    private Session session;
    private ChannelSftp channelSftp;

    private final String remoteFilePath = AppConfig.get("sftp.remote.path");
    private final String localDownloadDir = AppConfig.get("local.download.dir");

    public boolean connect(String host, Integer port, String user, String password) {
        try {
            JSch jsch = new JSch();
            session = jsch.getSession(user, host, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();
            downloadJson();
            return true;
        } catch (JSchException e) {
            disconnect();
            throw new RuntimeException(e);
        } catch (SftpException e) {
            throw new RuntimeException(e);
        }
    }

    public void downloadJson() throws SftpException {
        channelSftp.get(remoteFilePath, localDownloadDir);
    }

    public void disconnect() {
        session.disconnect();
        channelSftp.disconnect();
    }
}
