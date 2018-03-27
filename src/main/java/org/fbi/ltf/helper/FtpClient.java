package org.fbi.ltf.helper;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: zhangxiaobo
 * Date: 13-2-4
 * Time: 下午4:22
 * To change this template use File | Settings | File Templates.
 */
public class FtpClient {
    private static Logger logger = LoggerFactory.getLogger(FtpClient.class);
    private FTPClient ftp;

    public FtpClient(String ip, String username, String pwd) {
        ftp = new FTPClient();
        String systemKey = FTPClientConfig.SYST_UNIX;
        String serverLanguageCode = "zh";
        FTPClientConfig config = new FTPClientConfig(systemKey);
        config.setServerLanguageCode(serverLanguageCode);
//        ftp.setControlKeepAliveTimeout(300);
        ftp.configure(config);
        try {
            ftp.connect(ip);
            logger.info("Connected to " + ip);
            logger.info("ftp reply string: " + ftp.getReplyString());
            // After connection attempt, you should check the reply code to verify
            int reply = ftp.getReplyCode();
            logger.info("Ftp reply code : " + reply);
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                logger.error("FTP server refused connection.");
            }
            ftp.login(username, pwd);
//            ftp.enterLocalPassiveMode();
            ftp.enterLocalActiveMode();;
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public String readFile(String remotePath, String fileName) throws IOException {

        if (!StringUtils.isEmpty(remotePath)) {
            logger.info("change directory to remote path: " + remotePath + "/" + fileName);
            // 改变工作路径
            if (!ftp.changeWorkingDirectory(remotePath)) {
                throw new RuntimeException("changeDirectory to remote path failed.目录不存在.");
            }
        }

        if (StringUtils.isEmpty(fileName)) {
            throw new RuntimeException("要读取的ftp文件名不能为空。");
        }

        StringBuilder msg = new StringBuilder();
        // 列出当前工作路径下的文件列表
        List<FTPFile> fileList = Arrays.asList(ftp.listFiles());
        if (fileList == null || fileList.size() == 0) {
            logger.error("no files in ftp server.");
            return msg.toString();
        }
        for (FTPFile ftpfile : fileList) {
            if (ftpfile.getName().equals(fileName)) {
                logger.info("----  start read file : " + fileName + "------");
                InputStream inputStream = ftp.retrieveFileStream(ftpfile.getName());
                InputStreamReader inputsr = new InputStreamReader(inputStream);
                BufferedReader br = new BufferedReader(inputsr);
                String line = null;
                while ((line = br.readLine()) != null) {
                    if (!StringUtils.isEmpty(line)) {
                        msg.append(line);
                    }
                }
                br.close();
                inputsr.close();
                inputStream.close();
                break;
            }
        }
        return msg.length() > 1 ? msg.substring(0, msg.length() - 1) : "";
    }

    public boolean uploadFile(String remotePath, String localPath, String fileName) throws IOException {
        boolean result = false;
        InputStream input = null;
        try {
            File file = new File(localPath + fileName);
            if (!file.exists()) {
                throw new RuntimeException("文件不存在。");
            }
            input = new FileInputStream(file);
            if (!StringUtils.isEmpty(remotePath)) {
                logger.info("change directory to remote path: " + remotePath + "/" + fileName);
                // 改变工作路径
                if (!ftp.changeWorkingDirectory(remotePath)) {
                    throw new RuntimeException("changeDirectory to remote path failed.目录不存在.");
                }
            }
            result = ftp.storeFile(fileName, input);
            if (!result) {
                throw new RuntimeException("文件上传失败!");
            }
        } catch (IOException e) {
            throw e;
        } finally {
            if (input != null) {
                input.close();
            }
        }
        return result;
    }

    public void logout() throws IOException {
        ftp.logout();
        if (ftp.isConnected()) {
            ftp.disconnect();
        }
    }
}