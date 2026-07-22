package org.example;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SftpService {

    private Session session;
    private ChannelSftp channelSftp;

    private final String remoteFilePath = AppConfig.get("sftp.remote.path");
    private final String localDownloadDir = AppConfig.get("local.download.dir");
    private final String jsonFilename = AppConfig.get("local.hosts.filename");

    public void connect(String host, int port, String user, String password) throws JSchException {
        JSch jsch = new JSch();
        session = jsch.getSession(user, host, port);
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");
        session.setTimeout(10_000);
        session.connect();

        channelSftp = (ChannelSftp) session.openChannel("sftp");
        channelSftp.connect();
    }

    public void downloadJson() throws SftpException, IOException {
        Path localDir = Paths.get(localDownloadDir);
        Files.createDirectories(localDir);

        String localPath = localDir.resolve(jsonFilename).toString();
        channelSftp.get(remoteFilePath, localPath);
    }

    public void uploadFile() throws SftpException {
        String localPath = Paths.get(localDownloadDir, jsonFilename).toString();
        channelSftp.put(localPath, remoteFilePath, ChannelSftp.OVERWRITE);
    }

    public void disconnect() {
        if (channelSftp != null && channelSftp.isConnected()) {
            channelSftp.disconnect();
        }
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }
}