package com.devicemanagement.log;

import org.apache.log4j.Logger;

public class MyLogger {
    private final static boolean logFlag = true;
    /**
     * 日志文件
     */
    private static Logger logger = Logger.getLogger("jrhb");
    //public final static String tag = "[" + Constance.APP_NAME + "]";
    private static String userName = "dd";

    public MyLogger(String userName) {
        this.userName = userName;
    }

    /**
     * 获取方法名
     * @return
     */
    private static String getFunctionName() {
        StackTraceElement[] sts = Thread.currentThread().getStackTrace();
        if (sts == null) {
            return null;
        }
        for (StackTraceElement st : sts) {
            if (st.isNativeMethod()) {
                continue;
            }
            if (st.getClassName().equals(Thread.class.getName())) {
                continue;
            }
            if (st.getClassName().equals(MyLogger.class.getName())) {
                continue;
            }
            return "[ "
                    + Thread.currentThread().getName() + ": "
                    + st.getFileName().replace(".java","") + ":" + st.getLineNumber() + " ]";
        }
        return null;
    }

    public static void info(String str) {
        if (logFlag) {
            String name = getFunctionName();
            if (name != null) {
                logger.info(name + " - " + str);
            } else {
                logger.info(str.toString());
            }
        }
    }

    public static void debug(String str) {
        if (logFlag) {
            String name = getFunctionName();
            if (name != null) {
                logger.debug(name + " - " + str);
            } else {
                logger.debug(str.toString());
            }
        }
    }

    public static void trace(String str) {
        if (logFlag) {
            String name = getFunctionName();
            if (name != null) {
                logger.trace(name + " - " + str);
            } else {
                logger.trace(str.toString());
            }
        }
    }

//    public static void w(String str) {
//        if (logFlag) {
//            String name = getFunctionName();
//            if (name != null) {
//                Log.w(tag, name + " - " + str);
//            } else {
//                Log.w(tag, str.toString());
//            }
//        }
//    }

    public static void error(String str) {
        if (logFlag) {
            String name = getFunctionName();
            if (name != null) {
                logger.error(name + " - " + str);
            } else {
                logger.error(str.toString());
            }
        }
    }


}
