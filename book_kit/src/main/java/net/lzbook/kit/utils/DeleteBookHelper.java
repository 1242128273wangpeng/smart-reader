package net.lzbook.kit.utils;

import net.lzbook.kit.book.component.service.DeleteBookIntentService;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class DeleteBookHelper {
    final static boolean isCheckDate = false;// TODO 测试修改为false
    final static boolean isAM = true;// TODO 测试时 根据SERVICE_START_HOUR 修改
    final static boolean isTEST = false;// TODO 测试修改 false
    private final static int SERVICE_START_HOUR = 3; // TODO 12小时制 测试时修改时间
    private final static int SERVICE_START_HOUR_OF_DAY = 3; // TODO 24小时制，测试时修改时间
    private final static int SERVICE_START_MINUTES = 0;
    private final static int SERVICE_START_SECOND = 0;
    private final static int SERVICE_START_MILLISECOND = 0;
    private final static int ALARM_TYPE = AlarmManager.RTC;
    private final static int SERVICE_STOP_HOUR = 4; // TODO 12小时制 测试时修改时间
    private final static int SERVICE_STOP_HOUR_OF_DAY = 4; // TODO 24小时制，测试时修改时间
    final String TAG = "DeletebookServiceHelper";
    private final long INTERVAL_DAY = AlarmManager.INTERVAL_DAY;
    Context mContext;
    FileViewer fileViewer;
    long INTERVAL_DAY_TEST = 2 * 60 * 1000;

    public DeleteBookHelper(Context context) {
        mContext = context;
        init();
    }

    public void init() {
        fileViewer = new FileViewer(mContext);
    }

    // ===========================================================================
    //
    // =======================================================================
    public void doDeleteHelper() {
        if (isTimeEffective(mContext)) {// TODO 执行删除前确认时间范围是否有效
            AppLog.d(TAG, "当前时间小于设定的最大时间，执行删除");
            fileViewer.doDelete();
        } else {
            AppLog.d(TAG, "当前时间大于设定的最大时间，不执行删除");
        }
    }

    public void doRemoveCallbackHelper() {
        fileViewer.setDeleteFailedCallback(new FileViewer.FileDeleteFailedCallback() {

            @Override
            public void getDeleteFailedPath(ArrayList<String> pathList) {
                if (pathList != null) {
                    if (pathList.size() == 0) {
                        AppLog.d(TAG, "没有删除失败的文件，移除线程回调");
                        if (fileViewer != null) {
                            fileViewer.removeDeleteCall();
                        }
                    }
                }
            }
        });
    }

    public void startPendingService() {
        Intent localIntent = new Intent(mContext, DeleteBookIntentService.class);
        localIntent.setAction(DeleteBookIntentService.ACTION_DO_DELETE);
        PendingIntent pendingIntent = PendingIntent.getService(mContext, 0, localIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        // 取消以前同类型的提醒
        alarmManager.cancel(pendingIntent);
        Date settedDate = getCalendarDate(mContext, alarmManager);
        if (isTEST) {
            alarmManager.setRepeating(ALARM_TYPE, settedDate.getTime(), INTERVAL_DAY_TEST, pendingIntent);
        } else {
            // 设定每天在指定的时间运行
            try {
                alarmManager.setRepeating(ALARM_TYPE, settedDate.getTime(), INTERVAL_DAY, pendingIntent);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    private Date getCalendarDate(Context context, AlarmManager alarmManager) {
        Calendar calendar = Calendar.getInstance();

        setCalendarHour(context, calendar);

        calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) - 1);
        calendar.set(Calendar.MINUTE, SERVICE_START_MINUTES);
        calendar.set(Calendar.SECOND, SERVICE_START_SECOND);
        calendar.set(Calendar.MILLISECOND, SERVICE_START_MILLISECOND);
//		calendar.setTimeInMillis(calendar.getTimeInMillis());

        AppLog.d(TAG, "MINUTE  " + calendar.get(Calendar.MINUTE));
        AppLog.d(TAG, "DAY_OF_MONTH  " + calendar.get(Calendar.DAY_OF_MONTH));
        AppLog.d(TAG, "设定的时间 getTimeInMillis：  " + calendar.getTimeInMillis());

        Date date = getRealTime(calendar);
        return date;

    }

    public boolean isTimeEffective(Context context) {
        Date maxDate = null;
        Calendar calendar = Calendar.getInstance();

        if (AppUtils.getTimeFormat(context).equals("24")) { // 24小时制
            calendar.add(Calendar.HOUR_OF_DAY, 24);
            calendar.set(Calendar.HOUR_OF_DAY, SERVICE_STOP_HOUR_OF_DAY);
            AppLog.d(TAG, "24小时格式，maxHour:  " + calendar.get(Calendar.HOUR_OF_DAY));
        } else {
            calendar.add(Calendar.HOUR, 12);// 12小时制
            calendar.set(Calendar.HOUR, SERVICE_STOP_HOUR);
            if (isAM) {
                calendar.set(Calendar.AM_PM, Calendar.AM);
            } else {
                calendar.set(Calendar.AM_PM, Calendar.PM);
            }
            AppLog.d(TAG, "12小时格式，maxHour:  " + calendar.get(Calendar.HOUR));
        }
        calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) - 1);
        AppLog.d(TAG, "当前的DAY_OF_MONTH  " + calendar.get(Calendar.DAY_OF_MONTH));
        maxDate = calendar.getTime();
        if (new Date().before(maxDate)) {// 当前时间小于设定的最大时间，仍然有效
            AppLog.d(TAG, "当前时间小于设定的最大时间 " + new Date().before(maxDate));
            return true;
        }
        return false;
    }

    /**
     * 设定小时
     *
     * calendar
     */
    protected void setCalendarHour(Context context, Calendar calendar) {
        if (AppUtils.getTimeFormat(context).equals("24")) { // 24小时制
            calendar.add(Calendar.HOUR_OF_DAY, 24);
            calendar.set(Calendar.HOUR_OF_DAY, SERVICE_START_HOUR_OF_DAY);
            AppLog.d(TAG, "HOUR_OF_DAY  " + calendar.get(Calendar.HOUR_OF_DAY));
            AppLog.d(TAG, "24小时格式，Hour:  " + calendar.get(Calendar.HOUR_OF_DAY));
        } else {
            calendar.add(Calendar.HOUR, 12);// 12小时制
            calendar.set(Calendar.HOUR, SERVICE_START_HOUR);
            if (isAM) {
                calendar.set(Calendar.AM_PM, Calendar.AM);
            } else {
                calendar.set(Calendar.AM_PM, Calendar.PM);
            }

            AppLog.d(TAG, "HOUR  " + calendar.get(Calendar.HOUR));
            AppLog.d(TAG, "AM_PM  " + calendar.get(Calendar.AM_PM));
            AppLog.d(TAG, "12小时格式，Hour:  " + calendar.get(Calendar.HOUR));
            AppLog.d(TAG, "12小时格式，am:pm?:  "
                    + (calendar.get(Calendar.AM_PM) == Calendar.AM ? Calendar.AM + " 上午" : Calendar.PM + " 下午"));
        }
    }

    /**
     * 真实时间
     *
     * calendar
     */
    public Date getRealTime(Calendar calendar) {
        Date date = null;
        if (calendar != null) {
            date = calendar.getTime();
            if (date.before(new Date())) {// 设定的时间小于当前时间
                date = AppUtils.addDay(date, 1);
            }
            AppLog.d(TAG, "设定的删除服务执行的时间 ： " + date.getTime());
        }
        return date;
    }

}
