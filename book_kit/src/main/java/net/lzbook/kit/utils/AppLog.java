package net.lzbook.kit.utils;

import android.text.TextUtils;
import android.util.Log;

import net.lzbook.kit.constants.Constants;

import java.util.Locale;


/**
 * Log打印工具类
 */
public class AppLog {

    /**
     * 是否显示日志
     */
    private static boolean showLog = Constants.SHOW_LOG;

    private static CustomLogger customLogger;

    public static void d(String content) {
        if (showLog) {
            StackTraceElement caller = getCallerStackTraceElement();
            String tag = generateTag(caller);
            if (customLogger != null) {
                customLogger.d(tag, "【" + content + "】");
            } else {
                Log.d(tag, "【" + content + "】");
            }

        }
    }

    public static void d(String content, Throwable tr) {
        if (showLog) {
            StackTraceElement caller = getCallerStackTraceElement();
            String tag = generateTag(caller);
            if (customLogger != null) {
                customLogger.d(tag, "【" + content + "】", tr);
            } else {
                Log.d(tag, "【" + content + "】", tr);
            }

        }
    }

    public static void e(String content) {
        if (showLog) {
            StackTraceElement caller = getCallerStackTraceElement();
            String tag = generateTag(caller);
            if (customLogger != null) {
                customLogger.e(tag, "【" + content + "】");
            } else {
                Log.e(tag, "【" + content + "】");
            }

        }
    }

    public static void e(String content, Throwable tr) {
        if (showLog) {
            StackTraceElement caller = getCallerStackTraceElement();
            String tag = generateTag(caller);
            if (customLogger != null) {
                customLogger.e(tag, "【" + content + "】", tr);
            } else {
                Log.e(tag, "【" + content + "】", tr);
            }

        }
    }

    public static void i(String content) {
        if (showLog) {
            StackTraceElement caller = getCallerStackTraceElement();
            String tag = generateTag(caller);
            if (customLogger != null) {
                customLogger.i(tag, "【" + content + "】");
            } else {
                Log.i(tag, "【" + content + "】");
            }

        }
    }

    public static void i(String content, Throwable tr) {
        if (showLog) {
            StackTraceElement caller = getCallerStackTraceElement();
            String tag = generateTag(caller);
            if (customLogger != null) {
                customLogger.i(tag, "【" + content + "】", tr);
            } else {
                Log.i(tag, "【" + content + "】", tr);
            }

        }
    }

    public static void v(String content) {
        if (showLog) {
            StackTraceElement caller = getCallerStackTraceElement();
            String tag = generateTag(caller);
            if (customLogger != null) {
                customLogger.v(tag, "【" + content + "】");
            } else {
                Log.v(tag, "【" + content + "】");
            }

        }
    }

    public static void v(String content, Throwable tr) {
        if (showLog) {
            StackTraceElement caller = getCallerStackTraceElement();
            String tag = generateTag(caller);
            if (customLogger != null) {
                customLogger.v(tag, "【" + content + "】", tr);
            } else {
                Log.v(tag, "【" + content + "】", tr);
            }

        }
    }

    public static void w(String content) {
        if (showLog) {
            StackTraceElement caller = getCallerStackTraceElement();
            String tag = generateTag(caller);
            if (customLogger != null) {
                customLogger.w(tag, "【" + content + "】");
            } else {
                Log.w(tag, "【" + content + "】");
            }

        }
    }

    public static void w(String content, Throwable tr) {
        if (showLog) {
            StackTraceElement caller = getCallerStackTraceElement();
            String tag = generateTag(caller);
            if (customLogger != null) {
                customLogger.w(tag, "【" + content + "】", tr);
            } else {
                Log.w(tag, "【" + content + "】", tr);
            }

        }
    }

    public static void w(Throwable tr) {
        if (showLog) {
            StackTraceElement caller = getCallerStackTraceElement();
            String tag = generateTag(caller);
            if (customLogger != null) {
                customLogger.w(tag, tr);
            } else {
                Log.w(tag, tr);
            }

        }
    }


    /**
     * 获取包名，方法名
     *
     * @param caller 追踪器
     * @return 打印路径
     */
    private static String generateTag(StackTraceElement caller) {
        String customTagPrefix = "";
        String tag = "%s.%s(L:%d)";
        String callerClazzName = caller.getClassName();
        callerClazzName = callerClazzName.substring(callerClazzName.lastIndexOf(".") + 1);
        tag = String.format(Locale.getDefault(), tag, callerClazzName, caller.getMethodName(), caller.getLineNumber());
        tag = TextUtils.isEmpty(customTagPrefix) ? tag : customTagPrefix + ":" + tag;
        return tag;
    }

    private static StackTraceElement getCallerStackTraceElement() {
        return Thread.currentThread().getStackTrace()[4];
    }


    public interface CustomLogger {

        void d(String var1, String var2);

        void d(String var1, String var2, Throwable var3);

        void e(String var1, String var2);

        void e(String var1, String var2, Throwable var3);

        void i(String var1, String var2);

        void i(String var1, String var2, Throwable var3);

        void v(String var1, String var2);

        void v(String var1, String var2, Throwable var3);

        void w(String var1, String var2);

        void w(String var1, String var2, Throwable var3);

        void w(String var1, Throwable var2);

    }

    //=================================萌萌哒分割线=================================

    /**
     * 一般信息
     * tag
     * msg
     */
    public static void i(String tag, String msg) {
        if (showLog)
            Log.i(tag, msg);
    }

    /**
     * 错误信息
     * tag
     * msg
     */
    public static void e(String tag, String msg) {
        if (showLog)
            Log.e(tag, msg);
    }

    /**
     * 错误信息
     * tag
     * msg
     * tr
     */
    public static void e(String tag, String msg, Throwable tr) {
        if (showLog)
            Log.e(tag, msg, tr);
    }


    /**
     * 警告信息.
     * tag
     * msg
     */
    public static void w(String tag, String msg) {
        if (showLog)
            Log.w(tag, msg);
    }

    /**
     * 警告信息.
     * tag
     * msg
     */
    public static void w(String tag, String msg, Throwable tr) {
        if (showLog)
            Log.w(tag, msg, tr);
    }

    /**
     * debug信息.
     * tag
     * msg
     */
    public static void d(String tag, String msg) {
        if (showLog)
            Log.d(tag, msg);
    }

    /**
     * 详细信息
     * tag
     * msg
     */
    public static void v(String tag, String msg) {
        if (showLog)
            Log.v(tag, msg);
    }
}
