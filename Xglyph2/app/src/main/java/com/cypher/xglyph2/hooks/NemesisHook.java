package com.cypher.xglyph2.hooks;

import static com.cypher.xglyph2.Xglyph.*;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

public class NemesisHook extends XC_MethodHook {

	private final XC_LoadPackage.LoadPackageParam b;

	public NemesisHook(XC_LoadPackage.LoadPackageParam b) {
		super();
		this.b = b;
	}


	@Override
	protected void afterHookedMethod(MethodHookParam param) throws Throwable {
		final Class<?> turingClass;

		try {
			turingClass = findClass(turingClassName, b.classLoader);
		} catch (XposedHelpers.ClassNotFoundError e) {
			log(TAG, turingClassName + ": class not found", true);
			return;
		}

		try {
			findAndHookMethod(turingClass, turingClassMethodName1, String.class, new TranslateGlyphsHook());
		} catch (NoSuchMethodError error) {
			log(TAG, turingClassName + "." + turingClassMethodName1 + ": method not found", true);
		}

		try {
			findAndHookMethod(turingClass, turingClassMethodName2, new ClearGlyphsHook());
		} catch (NoSuchMethodError error) {
			log(TAG, turingClassName + "." + turingClassMethodName2 + ": method not found", true);
		}

	}
}
