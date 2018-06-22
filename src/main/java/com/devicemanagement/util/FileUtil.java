package com.devicemanagement.util;

import android.os.Environment;
import android.util.Log;


import com.devicemanagement.log.ConfiguratorLog;
import com.devicemanagement.log.MyLogger;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author lg
 * some methods for dealing with files.
 */
public class FileUtil {

    public static final SimpleDateFormat sdf = new SimpleDateFormat(
            "yyyy-MM-dd_HH_mm_ss", Locale.CHINA);

    public static final SimpleDateFormat simpleDayFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);

    public static final long ONE_MONTH = 60 * 24 * 60 * 60 * 1000L;
    public static final long THREE_DAY_AGO = 3 * 24 * 60 * 60 * 1000L;

    public static File[] listFilesByPath( final String path){
        File dirFile = null;
        String filePath = path;
        try {
            if (!filePath.endsWith(File.separator)) {
                filePath = filePath + File.separator;
            }
            dirFile = new File(filePath);
            if (!dirFile.exists() || !dirFile.isDirectory()) {
                return null;
            }
        }catch (Exception e){
            return null;
        }
        return dirFile.listFiles();
    }

    /**
     * 删除文件夹以及目录下的文件
     * @param   filePath 被删除目录的文件路径
     * @return  目录删除成功返回true，否则返回false
     */
    public static boolean deleteOldFile(String filePath) {
        boolean flag = false;
        //如果filePath不以文件分隔符结尾，自动添加文件分隔符
        if (!filePath.endsWith(File.separator)) {
            filePath = filePath + File.separator;
        }
        File dirFile = new File(filePath);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        flag = true;
        File[] files = dirFile.listFiles();
        //gLogger.info(files[0].getAbsolutePath());
        //遍历删除文件夹下的三天之前的文件
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile() &&isOldFile(files[i], THREE_DAY_AGO)) {
                //删除子文件
                flag = files[i].delete();
            }
        }
        return flag;
    }

    /**
     * 判断file是否是三天前的文件
     * @param file : log file
     * @param time :
     * @return
     */
    public static boolean isOldFile(File file, long time){
        String filename = file.getName();
        if(filename.length()<10) return false;
        try{
            String prefileName = filename.substring(0,10);
            SimpleDateFormat ft = ConfiguratorLog.getFt();
            Date old_date = ft.parse(prefileName);
            if(System.currentTimeMillis() - old_date.getTime() >= time)
                return true;
            else return false;
        }catch (Exception e){
            e.printStackTrace();
            MyLogger.error(e.toString());
            return false;
        }
    }


    /**
     * 判断是否是一天前的文件
     * @param file 要判断的文件 文件名格式为（YYYY-mm-dd）
     * @return
     */
    public static boolean isOneDayAgoFile(File file){
        String filename = file.getName();
        if(filename.length()<10) return false;
        try{
            String prefileName = filename.substring(0,10);
            SimpleDateFormat ft = ConfiguratorLog.getFt();
            Date old_date = ft.parse(prefileName);
            if(System.currentTimeMillis() - old_date.getTime() >= ConfiguratorLog.ONE_DAY_AGO &&
                    System.currentTimeMillis() - old_date.getTime() < ConfiguratorLog.ONE_AND_A_HALF_DAYS)
                return true;
            else return false;
        }catch (Exception e){
            e.printStackTrace();
            MyLogger.error(e.toString());
            return false;
        }
    }

    /**
     * delete the file which name is endsWith apk under the {@code path} directory.
     * @param path the path in which user want to delete the file.
     * @param endName the file's endsWith String.
     * @return whether if delete all file under the path directory succeed.
     */
    public static boolean deleteOldVersionApk(String path, String endName){
        //如果filePath不以文件分隔符结尾，自动添加文件分隔符
        boolean flag = false;
        if (!path.endsWith(File.separator)) {
            path = path + File.separator;
        }
        File dirFile = new File(path);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        File[] files = dirFile.listFiles();
        for(int i = 0;i < files.length; i++){
            if(files[i].getName().endsWith(endName)){
                flag = files[i].delete();
            }
        }
        return flag;
    }

    /**
     * 取出filePath目录下的当天日志文件
     * @param   filePath 要查找文件的路径
     * @return  目录查找成功返回包含的文件，否则返回null
     */
    public static List<File> findCurDayLog(String filePath) {
        List<File> uploadFiles = new ArrayList<>();
        //如果filePath不以文件分隔符结尾，自动添加文件分隔符
        if (!filePath.endsWith(File.separator)) {
            filePath = filePath + File.separator;
        }
        File dirFile = new File(filePath);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return null;
        }
        File[] files = dirFile.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile() && isCurDayLog(files[i])) {
                uploadFiles.add(files[i]);
            }
        }
        if(uploadFiles.size()>0)
            return uploadFiles;
        else return null;
    }

    public static boolean isCurDayLog(File file){
        String filename = file.getName();
        if(filename.length()<10) return false;
        try{
            String prefileName = filename.substring(0,10);
            SimpleDateFormat ft = ConfiguratorLog.getFt();
            Date date = new Date();
            String nowString = ft.format(date);
            if(prefileName.equals(nowString))
                return true;
            else return false;
        }catch (Exception e){
            MyLogger.error(e.toString());
            return false;
        }
    }

    /**
     * 找到 {@code filepath} 目录下所有的文件
     * @param filePath 文件目录的绝对路径
     * @return
     */
    public static List<File> findAllLog(String filePath){
        List<File> uploadFiles = new ArrayList<>();
        //如果filePath不以文件分隔符结尾，自动添加文件分隔符
        if (!filePath.endsWith(File.separator)) {
            filePath = filePath + File.separator;
        }
        File dirFile = new File(filePath);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return null;
        }
        File[] files = dirFile.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                uploadFiles.add(files[i]);
            }
        }
        if(uploadFiles.size()>0)
            return uploadFiles;
        else return null;
    }

    public static boolean clearPictures(String filePath) throws Exception{
        //如果filePath不以文件分隔符结尾，自动添加文件分隔符
        boolean flag = false;
        Date date = new Date();
        if (!filePath.endsWith(File.separator)) {
            filePath = filePath + File.separator;
        }
        File dirFile = new File(filePath);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        File[] files = dirFile.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile() && isOneMonthAgoPictures(files[i],date)) {
               flag = files[i].delete();
            }
        }
        return flag;
    }

    public static boolean isOneMonthAgoPictures(File file, Date date) throws Exception{
        String filename = file.getName();
        Date fileDate = null;
        try {
            if (!filename.contains(".jpeg")) return false;
            String str = filename.substring(16, 35);
            fileDate = sdf.parse(str);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return  date.getTime() - fileDate.getTime() >= ONE_MONTH;
    }

    public static final int MAX_H264_FILE_NUM = 400;
    public static final String VIDEO_FILE_NAME_SUFFIX = ".h264";
    public static final String VIDEO_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/rtmpvideo";

    /**
     * 参考设备存储空间的大小，删除过旧的视频文件
     * @param fileDir 文件夹目录路径
     * @param num 文件个数
     */
    public static boolean deleteOldH264File(String fileDir, int num){
        try {
            int delnum = num - MAX_H264_FILE_NUM;
            Log.e("FileUtil",delnum+"");
            int desnum = 0;
            File dirs = new File(fileDir);
            File[] dir = dirs.listFiles();
            sortFileByName(dir);
            for (File files : dir) {
                File[] delfiles = files.listFiles();
                for (File delfile : delfiles) {
                    if (delfile.isFile()) {
                        delfile.delete();
                        desnum++;
                        Log.e("FileUtil",desnum+"");
                        if (desnum > delnum) {
                            break;
                        }
                    }
                }
                if (desnum == delfiles.length) {
                    files.delete();
                }
                if (desnum > delnum) {
                    break;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 根据文件名(yyyy-mm-dd)对文件列表进行排序(从小到大)
     * @param files
     */
    public static void sortFileByName(File[] files){
        int i,j,k;
        String name;
        File temp;
        for(i = 0; i < files.length; i++){
            k = i;
            name = files[i].getName();
            for(j = i+1; j < files.length;j++){
                if(name.compareTo(files[j].getName()) > 0 ){
                    k = j;
                }
            }
            if(k != i){
                temp = files[i];
                files[i] = files[k];
                files[k] = temp;
            }
        }
    }

    /**
     * 返回指令文件夹目录下所有的文件个数（不包括子文件夹）
     * @param dirPath 文件夹目录路径
     * @return 文件个数
     */
    public static int getFileNums(String dirPath) throws Exception{
        File[] dirs = listFilesByPath(dirPath);
        int fileCount = 0;
        if(dirs != null) {
            for (File dir : dirs) {
                if (dir.isDirectory()) {
                    File[] files = listFilesByPath(dir.getPath());
                    if(files != null) {
                        for (File subfile : files) {
                            if(subfile.isFile()){   //&& subfile.getName().endsWith(VIDEO_FILE_NAME_SUFFIX)
                                fileCount++;
                            }
                        }
                    }
                }
            }
        }
        return fileCount;
    }

    /**
     * 根据日期删除视频文件  删除三天前的视频文件
     * @param dirPath 文件夹目录
     */
    public static void delH264FileByDate(String dirPath) throws Exception{
        File[] dirs = listFilesByPath(dirPath);
        if(dirs != null){
            for(File dir : dirs){
                if( dir.isDirectory() && isThreeDayAgoDir(dir)){
                    File[] files = listFilesByPath(dir.getPath());
                    if(files != null){
                        for(File file : files){
                            file.delete();
                        }
                    }
                    dir.delete();
                }
            }
        }
    }

    public static boolean isThreeDayAgoDir(File dir){
        Date curDate = new Date();
        Date date = null;
        try {
            date = simpleDayFormat.parse(dir.getName());
        }catch (Exception e){
            return false;
        }
        return curDate.getTime() - date.getTime() >= THREE_DAY_AGO;
    }

    public static byte[] bin2String(File file){
        if(file.isFile() && file.exists()) {
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                byte[] buffer = new byte[1024];
                List<Byte> listByte = new ArrayList<>();
                int count,i;
                while ((count = fileInputStream.read(buffer)) != -1) {
                    for(i = 0;i < count;i++) {
                        listByte.add(buffer[i]);
                    }
                }
                byte[] binByte = new byte[listByte.size()];
                for (i = 0; i < binByte.length; i++) {
                    binByte[i] = listByte.get(i);
                }
                MyLogger.info("length: "+binByte.length);
                return binByte;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 根据目录和文件后缀名，列出符合该条件的所有文件。
     * @param path 文件所在目录
     * @param suffix  文件后缀名
     * @return 文件的list列表
     */
    public static List<File> listFilesByDirAndSuffix(String path, String suffix){
        List<File> fileList = new ArrayList<>();
        //如果filePath不以文件分隔符结尾，自动添加文件分隔符
        if (!path.endsWith(File.separator)) {
            path = path + File.separator;
        }
        File dirFile = new File(path);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return null;
        }
        File[] files = dirFile.listFiles();
        for(File file: files){
            if(file.getName().endsWith(suffix)){
                fileList.add(file);
            }
        }
        return fileList;
    }

    /**
     * 根据文件名对文件列表进行排序
     * @param files
     */
    public static void sortFileByName(List<File> files){
        int i,j,k;
        String name;
        File temp;
        //List<File> sortFile = new ArrayList<>();
        for(i = 0; i < files.size(); i++){
            k = i;
            name = files.get(i).getName();
            for(j = i+1; j < files.size();j++){
                if(name.compareTo(files.get(j).getName()) > 0 ){
                    k = j;
                }
            }
            if(k != i){
                temp = files.get(i);
                files.set(i,files.get(k));
                files.set(k,temp);
            }
        }
    }
}
