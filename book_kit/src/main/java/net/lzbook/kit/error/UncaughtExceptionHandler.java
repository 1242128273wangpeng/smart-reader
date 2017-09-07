package net.lzbook.kit.error;

import android.os.Process;

import java.io.PrintWriter;
import java.io.StringWriter;

public class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    public UncaughtExceptionHandler() {
    }

    @Override
    public void uncaughtException(Thread thread, Throwable exception) {
        final StringWriter stackTrace = new StringWriter();
        exception.printStackTrace(new PrintWriter(stackTrace));
        System.err.println(stackTrace);
        Process.killProcess(Process.myPid());
        System.exit(10);
    }
}
