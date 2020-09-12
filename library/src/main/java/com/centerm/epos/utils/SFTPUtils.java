package com.centerm.epos.utils;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

/**
 * Sftp工具类
 * @author 16240
 */
public class SFTPUtils {

    /**
     * 上传成功
     */
    public static final int UPLOAD_SUCC = 1;
    /**
     * 上传失败
     */
    public static final int UP_LOAD_FAILED = 2;

    private static final String TAG="SFTPUtils";
    private String host;
    private int port;
    private String username;
    private String password;

    private static final String NO_SUCN_FILE = "no such file";
    /**
     * SFTP 服务器IP
     */
    public static final String SFTP_IP = "211.95.56.219";

    /**
     * SFTP 服务器备份IP
     */
    public static final String SFTP_IP_BACK = "180.168.120.30";
    /**
     * STFTP 服务端口
     */
    public static final int PORT = 65222;
    /**
     * 用户名 USER_NAME
     */
    public static final String USER_NAME = "poslog";

    /**
     * 用户密码 USER_PWD
     */
    public static final String USER_PWD = "w8mfek$h64HsPp5O";

    private ChannelSftp sftp = null;
    private Session sshSession = null;

    public SFTPUtils(String host, String username, String password) {
        this.host = host;
        this.username = username;
        this.password = password;
        this.port = PORT;
    }

    public SFTPUtils(String host, int port, String username, String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    /**
     * connect server via sftp
     */
    public ChannelSftp connect() {
        JSch jsch = new JSch();
        try {
            sshSession = jsch.getSession(username, host, port);
            sshSession.setPassword(password);
            sshSession.setConfig("PreferredAuthentications","password");
            Properties sshConfig = new Properties();
            sshConfig.put("StrictHostKeyChecking", "no");
            Log.d(TAG, "SFTUtils:"+String.format("Connecting to %s@%s:%s",username,host, PORT));
            sshSession.setConfig(sshConfig);
            sshSession.connect();
            Channel channel = sshSession.openChannel("sftp");
            if (channel != null) {
                channel.connect();
            } else {
                Log.e(TAG, "channel connecting failed.");
            }
            sftp = (ChannelSftp) channel;
        } catch (JSchException e) {
            e.printStackTrace();
        }
        return sftp;
    }


    /**
     * 断开服务器
     */
    public void disconnect() {
        if (this.sftp != null) {
            if (this.sftp.isConnected()) {
                this.sftp.disconnect();
                Log.d(TAG,"sftp is closed already");
            }
        }
        if (this.sshSession != null) {
            if (this.sshSession.isConnected()) {
                this.sshSession.disconnect();
                Log.d(TAG,"sshSession is closed already");
            }
        }
    }

    /**
     * 单个文件上传
     * @param remotePath
     * @param remoteFileName
     * @param localPath
     * @param localFileName
     * @return
     */
    public boolean uploadFile(String remotePath, String remoteFileName,
                              String localPath, String localFileName) {
        FileInputStream in = null;
        try {
            createDir(remotePath);
            System.out.println(remotePath);
            File file = new File(localPath +"/"+ localFileName);
            in = new FileInputStream(file);
            System.out.println(in);
            sftp.put(in, remoteFileName);
            System.out.println(sftp);
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (SftpException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    /**
     * 单个文件上传
     * @param remotePath
     * @param remoteFileName
     * @param localPath
     * @param localFileName
     * @return
     */
    public boolean uploadFile(String remotePath, String remoteFileName,
                              String localPath, String localFileName, Handler handler) {
        System.out.println("=============uploadFile()==============");
        FileInputStream in = null;
        try {
            Log.d("sftp!=null:",""+(sftp!=null));
            if(sftp!=null && !TextUtils.isEmpty(remotePath)){
                createDir(remotePath);
                System.out.println("remotePath:"+remotePath);
                File file = new File(localPath + localFileName);
                in = new FileInputStream(file);
                System.out.println(in);
                sftp.put(in, remoteFileName);
                System.out.println(sftp);
                handler.obtainMessage(UPLOAD_SUCC).sendToTarget();
                return true;
            }else{
                handler.obtainMessage(UP_LOAD_FAILED).sendToTarget();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            handler.obtainMessage(UP_LOAD_FAILED).sendToTarget();
        } catch (SftpException e) {
            e.printStackTrace();
            handler.obtainMessage(UP_LOAD_FAILED).sendToTarget();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    handler.obtainMessage(UP_LOAD_FAILED).sendToTarget();
                }
            }
        }
        return false;
    }

    /**
     * 批量上传
     * @param remotePath
     * @param localPath
     * @param del
     * @return
     */
    public boolean bacthUploadFile(String remotePath, String localPath,
                                   boolean del) {
        try {
            File file = new File(localPath);
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile()
                        && files[i].getName().indexOf("bak") == -1) {
                    synchronized(remotePath){
                        if (this.uploadFile(remotePath, files[i].getName(),
                                localPath, files[i].getName())
                                && del) {
                            deleteFile(localPath + files[i].getName());
                        }
                    }
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.disconnect();
        }
        return false;

    }

    /**
     * 批量下载文件
     *
     * @param remotPath
     *            远程下载目录(以路径符号结束)
     * @param localPath
     *            本地保存目录(以路径符号结束)
     * @param fileFormat
     *            下载文件格式(以特定字符开头,为空不做检验)
     * @param del
     *            下载后是否删除sftp文件
     * @return
     */
    @SuppressWarnings("rawtypes")
    public boolean batchDownLoadFile(String remotPath, String localPath,
                                     String fileFormat, boolean del) {
        try {
            connect();
            Vector v = listFiles(remotPath);
            if (v.size() > 0) {

                Iterator it = v.iterator();
                while (it.hasNext()) {
                    ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) it.next();
                    String filename = entry.getFilename();
                    SftpATTRS attrs = entry.getAttrs();
                    if (!attrs.isDir()) {
                        if (fileFormat != null && !"".equals(fileFormat.trim())) {
                            if (filename.startsWith(fileFormat)) {
                                if (this.downloadFile(remotPath, filename,
                                        localPath, filename)
                                        && del) {
                                    deleteSFTP(remotPath, filename);
                                }
                            }
                        } else {
                            if (this.downloadFile(remotPath, filename,
                                    localPath, filename)
                                    && del) {
                                deleteSFTP(remotPath, filename);
                            }
                        }
                    }
                }
            }
        } catch (SftpException e) {
            e.printStackTrace();
        } finally {
            this.disconnect();
        }
        return false;
    }

    /**
     * 单个文件下载
     * @param remotePath
     * @param remoteFileName
     * @param localPath
     * @param localFileName
     * @return
     */
    public boolean downloadFile(String remotePath, String remoteFileName,
                                String localPath, String localFileName) {
        try {
            sftp.cd(remotePath);
            File file = new File(localPath + localFileName);
            mkdirs(localPath + localFileName);
            sftp.get(remoteFileName, new FileOutputStream(file));
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (SftpException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * 删除文件
     * @param filePath
     * @return
     */
    public boolean deleteFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return false;
        }
        if (!file.isFile()) {
            return false;
        }
        return file.delete();
    }

    public boolean createDir(String createpath) {
        Log.d(TAG,"=============createpath()=========================");
        try {
            Log.d(TAG,"createpath:"+createpath);
            Log.d(TAG,"this.sftp!=null:"+(this.sftp!=null));
            if(this.sftp!=null) {
                Log.d(TAG,"isDirExist(createpath):"+(isDirExist(createpath)));
                if (isDirExist(createpath)) {
                    this.sftp.cd(createpath);
                    return true;
                }
                String[] pathArry = createpath.split("/");
                StringBuffer filePath = new StringBuffer("/");
                for (String path : pathArry) {
                    if ("".equals(path)) {
                        continue;
                    }
                    filePath.append(path + "/");
                    if (isDirExist(createpath)) {
                        sftp.cd(createpath);
                    } else {
                        sftp.mkdir(createpath);
                        sftp.cd(createpath);
                    }
                }
                this.sftp.cd(createpath);
                return true;
            }

        } catch (SftpException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 判断目录是否存在
     * @param directory
     * @return
     */
    @SuppressLint("DefaultLocale")
    public boolean isDirExist(String directory) {
        boolean isDirExistFlag = false;
        try {
            SftpATTRS sftpATTRS = sftp.lstat(directory);
            isDirExistFlag = true;
            return sftpATTRS.isDir();
        } catch (Exception e) {
            if (NO_SUCN_FILE.equals(e.getMessage().toLowerCase())) {
                isDirExistFlag = false;
            }
        }
        return isDirExistFlag;
    }

    public void deleteSFTP(String directory, String deleteFile) {
        try {
            sftp.cd(directory);
            sftp.rm(deleteFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建目录
     * @param path
     */
    public void mkdirs(String path) {
        File f = new File(path);
        String fs = f.getParent();
        f = new File(fs);
        if (!f.exists()) {
            f.mkdirs();
        }
    }

    /**
     * 列出目录文件
     * @param directory
     * @return
     * @throws SftpException
     */

    @SuppressWarnings("rawtypes")
    public Vector listFiles(String directory) throws SftpException {
        return sftp.ls(directory);
    }

}

