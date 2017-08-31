package net.lzbook.kit.utils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;

public class ATManager {
	private static List<Activity> activityList = new LinkedList<>();

	public static void addActivity(Activity activity) {
		activityList.add(activity);
	}

	public static void removeActivity(Activity activity) {
		activityList.remove(activity);
	}

	public static void exitClient() {
		if(activityList!=null){
			while (activityList.size()!=0){
				activityList.get(0).finish();
			}
			activityList.clear();
		}
	}
}
