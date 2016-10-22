package com.dekagames.gle;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Log {
    public static boolean NEED_LOGGING = true;

    private static Logger logger;

    static {
//        try {
        logger = Logger.getLogger(GLE.class.getName());
//            handler = new FileHandler("%h/dongle.log", false);
//            handler.setFormatter(new SimpleFormatter());
//            logger.addHandler(handler);
//        } catch (IOException e) {
//            logger.log(Level.SEVERE, "Do not create log file");
//            e.printStackTrace();
//        }
    }

    public static void info(String msg){
        if (!NEED_LOGGING) return;
        logger.log(Level.INFO, msg);
    }

    public static void error(String msg){
        if (!NEED_LOGGING) return;
        logger.log(Level.SEVERE, msg);
    }

    public static void exception(String msg, Exception e){
        logger.log(Level.SEVERE, msg, e);
    }

}
