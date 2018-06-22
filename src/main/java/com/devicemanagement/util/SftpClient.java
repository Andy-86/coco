package com.devicemanagement.util;

/**
 * Created by Administrator on 2017/7/4 0004.
 *
 */

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;
import java.util.Vector;


public class SftpClient {

    private static SftpClient sftpClient = null;
    private static ChannelSftp channelSftp = null;

    private static Logger gLogger = Logger.getLogger("SftpClient");

    /**
     * 获取SftpClient实例
     * @return
     */
    public static SftpClient getInStance(){
        if(sftpClient==null) {
            sftpClient = new SftpClient();
        }
        return sftpClient;
    }

    /**
     * 获取ChannelSftp实例
     * @param host
     * @param port
     * @param username
     * @param password
     * @return
     */
    public static boolean InitChannelSftp(String host, int port, String username,
                                          String password){
        channelSftp = getInStance().connect(host,port,username,password);
        if(channelSftp == null) return false;
        return true;
    }
    public static ChannelSftp getChannelSftp(){
        return channelSftp;
    }
    /**
     * 连接sftp服务器
     * @param host 主机
     * @param port 端口
     * @param username 用户名
     * @param password 密码
     * @return
     */
    public ChannelSftp connect(String host, int port, String username,
                               String password) {
        ChannelSftp sftp = null;
        try {
            JSch jsch = new JSch();
            jsch.getSession(username, host, port);
            Session sshSession = jsch.getSession(username, host, port);
            gLogger.info("Session created.");
            sshSession.setPassword(password);
            Properties sshConfig = new Properties();
            sshConfig.put("StrictHostKeyChecking", "no");
            sshSession.setConfig(sshConfig);
            sshSession.connect();
            gLogger.info("Session connected.");
            gLogger.info("Opening Channel.");
            Channel channel = sshSession.openChannel("sftp");
            channel.connect();
            sftp = (ChannelSftp) channel;
            gLogger.info("Connected to " + host + ".");
        } catch (Exception e) {
            e.printStackTrace() ;
        }
        return sftp;
    }

    /**
     * 上传文件
     * @param directory 上传的目录
     * @param uploadFile 要上传的文件
     * @param sftp
     */
    public void upload(String directory, String uploadFile, ChannelSftp sftp) {
        try {
            sftp.cd(directory);
            File file=new File(uploadFile);
            sftp.put(new FileInputStream(file), file.getName());
        } catch (Exception e) {
            e.printStackTrace();
            gLogger.error(e);
        }
    }

    /**
     * 下载文件
     * @param directory 下载目录
     * @param downloadFile 下载的文件
     * @param saveFile 存在本地的路径
     * @param sftp
     */
    public boolean download(String directory, String downloadFile, String saveFile, ChannelSftp sftp) {
        try {
            sftp.cd(directory);
            File file=new File(saveFile);
            sftp.get(downloadFile, new FileOutputStream(file));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            gLogger.error(e);
            return false;
        }
    }

    /**
     * 删除文件
     * @param directory 要删除文件所在目录
     * @param deleteFile 要删除的文件
     * @param sftp
     */
    public void delete(String directory, String deleteFile, ChannelSftp sftp) {
        try {
            sftp.cd(directory);
            sftp.rm(deleteFile);
        } catch (Exception e) {
            e.printStackTrace();
            gLogger.error(e);
        }
    }

    /**
     * 列出目录下的文件
     * @param directory 要列出的目录
     * @param sftp
     * @return
     * @throws SftpException
     */
    @SuppressWarnings("unchecked")
    public Vector<ChannelSftp.LsEntry> listFiles(String directory, ChannelSftp sftp) throws SftpException{
        return sftp.ls(directory);
    }

    /**
     * 关闭sftp连接
     */
    public void stopSftpConnect(){
        try{
            Session session=null;
            if(getChannelSftp()!=null) {
                session = getChannelSftp().getSession();
                getChannelSftp().disconnect();
            }
            if(session!=null){
                session.disconnect();
            }
            sftpClient=null;
            channelSftp=null;
        }catch (JSchException js){
            js.printStackTrace();
            gLogger.error(js);
            if(getChannelSftp()!=null) {
                getChannelSftp().disconnect();
                channelSftp=null;
                sftpClient=null;
            }
        }
    }

}
