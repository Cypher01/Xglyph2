package com.cypher.xglyph2.hooks;

import android.content.pm.ApplicationInfo;
import com.cypher.xglyph2.Xglyph;
import de.robv.android.xposed.XC_MethodHook;

import java.util.ArrayList;
import java.util.List;

import static com.cypher.xglyph2.Xglyph.*;

public class InstalledApplicationsHook extends XC_MethodHook {
	@Override
	protected void afterHookedMethod(MethodHookParam param) throws Throwable {
		debugLog(apmClassName + "." + apmClassMethodName1 + " called");

		List installedApplications = (List) param.getResult();

		ArrayList<ApplicationInfo> sortedOutApplications = new ArrayList<>();

		for (Object application : installedApplications) {
			ApplicationInfo applicationInfo = (ApplicationInfo) application;

			if (!applicationInfo.packageName.contains(Xglyph.class.getPackage().getName())) {
				sortedOutApplications.add(applicationInfo);
			}
		}

		param.setResult(sortedOutApplications);
	}
}
