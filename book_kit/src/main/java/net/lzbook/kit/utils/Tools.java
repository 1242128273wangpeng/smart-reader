package net.lzbook.kit.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tools {
    private static int[] count_rating = new int[]{4, 5};
    private static Random random = new Random();
    private static String userWord = "";

    /**
     * return int 4 or 5
     */
    public static int getIntRandom() {
        return count_rating[random.nextInt(2)];
    }

    /*
     * 去掉章节名中的序相关字符,用于判断是否有重复章节
     */
    public static String getPatterName(String name) {
        String m_patternString = "第[\\d廿两零一二三四五六七八九十百千]+[篇章节集部张卷回]";
        Pattern pattern = Pattern.compile(m_patternString);
        Matcher matcher = pattern.matcher(name);
        while (matcher.find()) {
            name = matcher.replaceAll("").trim();
        }
        return name;
    }

    /**
     * 字符是否为中文
     * <p/>
     * c
     */
    public static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
            return true;
        }
        return false;
    }

    /**
     * 获取热词
     * <p/>
     * list
     */
    public static void getHotWord(Context mContext, ArrayList<String> list) {
        try {
            list.clear();
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
            String hotWordStr = sp.getString("hotWord", "");
            if (hotWordStr.length() > 0) {
                String[] hotWords = hotWordStr.split("\\|");
                for (String hotWord : hotWords) {
                    list.add(hotWord);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取历史词
     * <p/>
     * list
     */
    public static ArrayList<String> getHistoryWord(Context mContext) {
        ArrayList<String> list = new ArrayList<String>();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        String historyKeyStr = sp.getString("search_history", "");
        if (historyKeyStr.length() > 0) {
            String[] historyKeys = historyKeyStr.split("\\|");
            int index = 0;
            for (String historyKey : historyKeys) {
                list.add(historyKey);
                index++;
                if (index > 29) { //搜索优化改版，保留最近搜索过的30条搜索历史
                    break;
                }
            }
        }
        return list;
    }

    public static void setUserSearchWord(String keyWord){
        userWord = keyWord;
    }

    public static String getKeyWord(){
        return userWord;
    }

    /**
     * 保存历史词
     * <p/>
     * list
     */
    public static void saveHistoryWord(Context mContext, ArrayList<String> mList) {
        if (mList == null) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (String historyWord : mList) {
            sb.append(historyWord);
            sb.append("|");
        }
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        sp.edit().putString("search_history", sb.toString().replaceAll(" ", "")).apply();
    }

    /**
     * 保存热词
     * <p/>
     * list
     */
    public static void saveHotWord(Context mContext, ArrayList<String> mList) {
        if (mList == null || mList.size() < 1) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (String hotWord : mList) {
            sb.append(hotWord);
            sb.append("|");
        }
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        sp.edit().putString("hotWord", sb.toString()).apply();
    }

    public static String compareTime(SimpleDateFormat formatter, long time) {
        String date = "";
        long l = System.currentTimeMillis() - time;
        if (l < 0) {
            return "刚刚";
        }
        long day = l / (24 * 60 * 60 * 1000);
        long hour = (l / (60 * 60 * 1000) - day * 24);
        long minute = ((l / (60 * 1000)) - day * 24 * 60 - hour * 60);

        if (day > 0) {
            if (day >= 1 && day <= 7) {
                date = day + "天前";
            } else {
                date = formatter.format(time);
            }
        } else if (hour > 0) {
            date = hour + "小时前";
        } else if (minute > 0) {
            date = minute + "分钟前";
        } else {
            date = "刚刚";
        }
        return date;
    }

    public static String logTime(SimpleDateFormat formatter, long time) {
        return formatter.format(time);
    }

    public static long transformTime(SimpleDateFormat simpleDateFormat, String time) {

        AppLog.e("TransformTime", "TransformTime: " + time);

        Date date = null;
        try {
            if (time.startsWith("今天")) {

                StringBuffer stringBuffer = new StringBuffer();

                Date currentDate = new Date();
                String currentTime = simpleDateFormat.format(currentDate);

                stringBuffer.append(currentTime).append(" ");

                time = time.substring(2);
                stringBuffer.append(time);

                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

                date = format.parse(stringBuffer.toString());

            } else if (time.startsWith("昨天")) {

                StringBuffer stringBuffer = new StringBuffer();

                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DATE, -1);
                String currentTime = simpleDateFormat.format(calendar.getTime());

                stringBuffer.append(currentTime).append(" ");

                time = time.substring(2);
                stringBuffer.append(time);

                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

                date = format.parse(stringBuffer.toString());
            } else if (time.endsWith("天前")) {
                int day = Integer.parseInt(time.substring(0, time.indexOf("天前")));
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DATE, -day);
                String currentTime = simpleDateFormat.format(calendar.getTime());
                date = simpleDateFormat.parse(currentTime);
            } else {
                date = simpleDateFormat.parse(time);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date.getTime();
    }

    public static String formatTime(long time) {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        return format.format(time);
    }


    public static String digest(String src) {
        try {
            byte[] btInput = src.getBytes("UTF-8");
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            mdInst.update(btInput);
            byte[] md = mdInst.digest();
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < md.length; i++) {
                int val = ((int) md[i]) & 0xff;
                if (val < 16)
                    sb.append("0");
                sb.append(Integer.toHexString(val));
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

}