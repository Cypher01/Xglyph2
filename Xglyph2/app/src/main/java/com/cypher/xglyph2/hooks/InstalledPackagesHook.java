package com.cypher.xglyph2.hooks;

import android.content.pm.PackageInfo;
import com.cypher.xglyph2.Xglyph;
import de.robv.android.xposed.XC_MethodHook;

import java.util.ArrayList;
import java.util.List;

import static com.cypher.xglyph2.Xglyph.*;

public class InstalledPackagesHook extends XC_MethodHook {
	@Override
	protected void afterHookedMethod(MethodHookParam param) throws Throwable {
		debugLog(apmClassName + "." + apmClassMethodName2 + " called");

		List installedPackages = (List) param.getResult();

		ArrayList<PackageInfo> sortedOutPackages = new ArrayList<>();

		for (Object installedPackage : installedPackages) {
			PackageInfo packageInfo = (PackageInfo) installedPackage;

			if (!packageInfo.packageName.contains(Xglyph.class.getPackage().getName())) {
				sortedOutPackages.add(packageInfo);
			}
		}

		param.setResult(sortedOutPackages);
	}
}
