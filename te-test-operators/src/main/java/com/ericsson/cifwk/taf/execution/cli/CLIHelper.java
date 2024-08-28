package com.ericsson.cifwk.taf.execution.cli;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.Ports;
import com.ericsson.cifwk.taf.execution.utils.FileHelper;
import com.ericsson.cifwk.taf.tools.cli.CLI;
import com.ericsson.cifwk.taf.tools.cli.CLIToolException;
import com.ericsson.cifwk.taf.tools.cli.jsch.JSchCLITool;
import com.google.common.base.Throwables;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

public class CLIHelper {

    public static void copyFileTo(String fromLocalFile, String toRemoteFile, Host te_master, Host te_slave) {
        Session session=null;
        ChannelExec channel = null;
        OutputStream out =null;
        InputStream in =null;
        try {
            session = connect(te_master.getIp(), getPort(te_master), te_master.getUser(), te_master.getPass());
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand("ssh " + te_slave.getIp());

            // send "C0644 filesize filename", where filename should not include '/'
            Path fromPath = FileHelper.getPath(fromLocalFile);
            File fromFile = fromPath.toAbsolutePath().normalize().toFile();
            String fromFilePath = fromFile.getPath();

            // get I/O streams for remote scp
            out = channel.getOutputStream();
            in = channel.getInputStream();
            channel.connect();

            String command = "scp -p -t " + toRemoteFile + "\n";
            out.write(command.getBytes());
            out.flush();
            if (checkAck(in) != 0) {
                throw new RuntimeException("Command execution error: " + command);
            }
            command = "T " + (fromFile.lastModified() / 1000) + " 0";
            // The access time should be sent here,
            // but it is not accessible with JavaAPI ;-<
            command += (" " + (fromFile.lastModified() / 1000) + " 0\n");
            out.write(command.getBytes());
            out.flush();
            if (checkAck(in) != 0) {
                throw new RuntimeException("Command execution error: " + command);
            }
            // send "C0644 filesize filename", where filename should not include '/'
            command = "C0644 " + fromFile.length() + " ";
            if (fromFilePath.lastIndexOf('/') > 0) {
                command += fromFilePath.substring(fromFilePath.lastIndexOf('/') + 1);
            } else {
                command += fromFilePath;
            }
            command += "\n";
            out.write(command.getBytes());
            out.flush();
            if (checkAck(in) != 0) {
                throw new RuntimeException("Command execution error: " + command);
            }
            // send a content
            FileInputStream fis = new FileInputStream(fromFile);
            byte[] buf = new byte[1024];
            while (true) {
                int len = fis.read(buf, 0, buf.length);
                if (len <= 0) break;
                out.write(buf, 0, len); //out.flush();
            }
            fis.close();
            // send '\0'
            buf[0] = 0;
            out.write(buf, 0, 1);
            out.flush();
            if (checkAck(in) != 0) {
                throw new RuntimeException("Can't close SCP");
            }
        }catch (Exception e) {
            throw Throwables.propagate(e);
        } finally {
            if (in!=null) {
                try {
                    in.close();
                } catch (Exception ignore) { //NOSONAR
                    // Ignore
                }
            }
            if (out!=null) {
                try {
                    out.close();
                } catch (Exception ignore) { //NOSONAR
                    // Ignore
                }
            }
            if (channel!=null) {
                try {
                    channel.disconnect();
                } catch (Exception ignore) { //NOSONAR
                    // Ignore
                }
            }
            if (session!=null) {
                try {
                    session.disconnect();
                } catch (Exception ignore) { //NOSONAR
                    // Ignore
                }
            }
        }
        //
    }

    static Session connect(String host, int port, String user, String password) throws JSchException, IOException {
        JSch jsch = new JSch();
        // jsch.setKnownHosts("/home/foo/.ssh/known_hosts");
        Session session = jsch.getSession(user, host, port);
        session.setPassword(password);
        // It must not be recommended, but if you want to skip host-key check, invoke following:
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect(JSchCLITool.DEFAULT_TIMEOUT_SEC * 1000);   // making a connection with timeout millisecond.
        return session;
    }

    static int getPort(Host host) {
        String port = host.getPort().get(Ports.SSH);
        if (port == null || port.trim().isEmpty()) return CLI.DEFAULT_SSH_PORT;
        try {
            return Integer.parseInt(port);
        } catch (NumberFormatException nfe) {
            throw new CLIToolException("Wrong number format for SSH port [" + port + "] in host " + host.getHostname(), nfe);
        }
    }

    static final int SUCCESS = 0;
    static final int ERROR = 1;
    static final int FATAL_ERROR = 2;
    static final int UNKNOWN = -1;

    static int checkAck(InputStream in) throws IOException {
        int b = in.read();
        if (b == SUCCESS) return SUCCESS;
        if (b == UNKNOWN) return UNKNOWN;
        if (b == ERROR || b == FATAL_ERROR) {
            StringBuffer sb = new StringBuffer();
            int c;
            while ((c = in.read()) != '\n') {
                sb.append((char) c);
            }
            throw new RuntimeException(sb.toString());
        }
        return UNKNOWN;
    }
}
