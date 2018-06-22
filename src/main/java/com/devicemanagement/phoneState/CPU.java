package com.devicemanagement.phoneState;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.regex.MatchResult;

/**
 * Created by Administrator on 2017/8/5 0005.
 *
 */
public class CPU {

    private static final String BOGOMIPS_PATTERN = "BogoMIPS[\\s]*:[\\s]*(\\d+\\.\\d+)[\\s]*\n";
    private static final String MEMTOTAL_PATTERN = "MemTotal[\\s]*:[\\s]*(\\d+)[\\s]*kB\n";
    private static final String MEMFREE_PATTERN = "MemFree[\\s]*:[\\s]*(\\d+)[\\s]*kB\n";
    public static boolean isLowMemory;//判断是否低内存运行
    /**
     * 获取内存信息
     * @return
     */
    public static String getMemoryInfo() {
        ProcessBuilder cmd;
        String result = "";
        try {
            String[] args = {"/system/bin/cat", "/proc/meminfo"};
            cmd = new ProcessBuilder(args);

            Process process = cmd.start();
            InputStream in = process.getInputStream();
            byte[] re = new byte[1024];
            while (in.read(re) != -1) {
                //System.out.println(new String(re));
                result = result + new String(re);
            }
            in.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return result;
    }

    /**
     * 主要是用于得到当前系统剩余内存的及判断是否处于低内存运行
     * @param context
     */
    private void displayBriefMemory(Context context) {
        final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(info);

        long availMem = info.availMem >> 10;

        boolean isLowMemory = info.lowMemory;

        long threshold = info.threshold >> 10;

//        Log.i(tag,"系统剩余内存:"+(info.availMem >> 10)+"k");
//
//        Log.i(tag,"系统是否处于低内存运行："+info.lowMemory);
//
//        Log.i(tag,"当系统剩余内存低于"+info.threshold+"时就看成低内存运行");

    }

    /**
     * 获取cpu信息
     */
    public static String getCPUinfo()
    {
        ProcessBuilder cmd;
        String result="";

        try{
            String[] args = {"/system/bin/cat", "/proc/cpuinfo"};
            cmd = new ProcessBuilder(args);

            Process process = cmd.start();
            InputStream in = process.getInputStream();
            byte[] re = new byte[1024];
            while(in.read(re) != -1){
                //System.out.println(new String(re));
                result = result + new String(re);
            }
            in.close();
        } catch(IOException ex){
            ex.printStackTrace();
        }
        return result;
    }

    /**
     * 获取cpu使用率
     * @return
     */
    public static int getProcessCpuRate() {

        StringBuilder tv = new StringBuilder();
        int rate = 0;

        try {
            String Result;
            Process p;
            p = Runtime.getRuntime().exec("top -n 1");

            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((Result = br.readLine()) != null) {
                if (Result.trim().length() < 1) {
                    continue;
                } else {
                    String[] CPUusr = Result.split("%");
                    tv.append("USER:" + CPUusr[0] + "\n");
                    String[] CPUusage = CPUusr[0].split("User");
                    String[] SYSusage = CPUusr[1].split("System");
                    tv.append("CPU:" + CPUusage[1].trim() + " length:" + CPUusage[1].trim().length() + "\n");
                    tv.append("SYS:" + SYSusage[1].trim() + " length:" + SYSusage[1].trim().length() + "\n");
                    rate = Integer.parseInt(CPUusage[1].trim()) + Integer.parseInt(SYSusage[1].trim());
                    break;
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println(rate + "");
        return rate;
    }

    /**
     * 获取cpu当前运行频率
     * @return
     * @throws Exception
     */
    public static int getCPUFrequencyCur() throws Exception {
        return readSystemFileAsInt("/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq");
    }

    private static int readSystemFileAsInt(final String pSystemFile) throws Exception {
        InputStream in = null;
        try {
            final Process process = new ProcessBuilder(new String[] {
                    "/system/bin/cat", pSystemFile }).start();

            in = process.getInputStream();
            final String content = readFully(in);
            return Integer.parseInt(content);
        } catch (final Exception e) {
            throw new Exception(e);
        }
    }

    public static final String readFully(final InputStream pInputStream) throws IOException {
        final StringBuilder sb = new StringBuilder();
        final Scanner sc = new Scanner(pInputStream);
        while(sc.hasNextLine()) {
            sb.append(sc.nextLine());
        }
        return sb.toString();
    }

    private static MatchResult matchSystemFile(final String pSystemFile,
                                               final String pPattern, final int pHorizon) throws Exception {
        InputStream in = null;
        try {
            final Process process = new ProcessBuilder(new String[]
                    { "/system/bin/cat", pSystemFile }).start();

            in = process.getInputStream();
            final Scanner scanner = new Scanner(in);

            final boolean matchFound = scanner.findWithinHorizon(pPattern, pHorizon) != null;
            if(matchFound) {
                return scanner.match();
            } else {
                throw new Exception();
            }
        } catch (final IOException e) {
            throw new Exception(e);
        }

    }
    /**
     * 主要是用于得到当前系统剩余内存，单位为M,指的是运行内存总的是1982M
     * @param context
     */
    public static float getAvailMemory(Context context) {
        final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(info);

//        return (int) info.availMem/a;//单位为M
        double a1=(info.availMem*1.0)/(info.totalMem*1.0);
//        double a2=info.availMem/info.totalMem;
        isLowMemory = info.lowMemory;
//        Log.d("CPU", "availMem: "+info.availMem);
//        Log.d("CPU", "totalMem: "+info.totalMem);
//        Log.d("CPU", "比例*100: "+a1*100);
//
//        Log.d("CPU", "剩余内存所占比重: "+a1+"是否低内存运行："+isLowMemory);
        return  (float) a1*100;//整数表示百分之多少
//        long availMem = info.availMem >> 10;
//
//
//        long threshold = info.threshold >> 10;

//        Log.i(tag,"系统剩余内存:"+(info.availMem >> 10)+"k");
//
//        Log.i(tag,"系统是否处于低内存运行："+info.lowMemory);
//
//        Log.i(tag,"当系统剩余内存低于"+info.threshold+"时就看成低内存运行");

    }

    /**
     * 获得SD卡总大小
     *
     * @return
     */
    public static String getSDTotalSize(Context context) {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return Formatter.formatFileSize(context, blockSize * totalBlocks);
    }

    /**
     * 获得sd卡剩余容量，即可用大小
     *
     * @return
     */
    public static short getSDAvailableSize(Context context) {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        Log.d("aaa", "blockSize: "+blockSize);
        Log.d("aaa", "availableBlocks: "+availableBlocks);
        Log.d("aaa", "blockSize * availableBlocks: "+blockSize * availableBlocks);
//        return Formatter.formatFileSize(context, blockSize * availableBlocks);//返回的是1.2G这种格式
        return (short) ((blockSize * availableBlocks)/(1024*1024));//返回的是M
    }

    /**
     * 获得机身内存总大小
     *
     * @return
     */
    public static String getRomTotalSize(Context context) {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return Formatter.formatFileSize(context, blockSize * totalBlocks);
    }

    /**
     * 获得机身可用内存
     *
     * @return
     */
    public static String getRomAvailableSize(Context context) {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return Formatter.formatFileSize(context, blockSize * availableBlocks);
    }
}

