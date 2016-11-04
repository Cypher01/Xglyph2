package com.cypher.xglyph2;

import com.cypher.xglyph2.hooks.*;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static com.cypher.xglyph2.MainActivity.*;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.*;

public class Xglyph implements IXposedHookLoadPackage {
	private static final String TAG = Xglyph.class.getSimpleName() + ": ";

	public static final XSharedPreferences pref = new XSharedPreferences(Xglyph.class.getPackage().getName(), PREF);

	public static final String moreGlyph1 = "ikj";
	public static final String moreGlyph2 = "jki";
	public static final String lessGlyph1 = "gkh";
	public static final String lessGlyph2 = "hkg";
	public static final String complexGlyph1 = "jkgh";
	public static final String complexGlyph2 = "hgkj";
	public static final String simpleGlyph1 = "ji";
	public static final String simpleGlyph2 = "ij";

	public static final String portalHackingParamsClassName = "com.nianticproject.ingress.shared.rpc.PortalHackingParams";
	public static final String userInputGlyphSequenceClassName = "com.nianticproject.ingress.glyph.UserInputGlyphSequence";
	public static final String glyphClassName = "com.nianticproject.ingress.glyph.Glyph";
	public static final String turingClassName = "com.nianticproject.ingress.common.utility.Turing";
	public static final String turingClassMethodName1 = "g";
	public static final String turingClassMethodName2 = "l";
	public static String speedClassName = "o.nb"; // FIXME: this class name is for Ingress v1.108.1 and maybe future versions
	public static String speedClassMethodName = "ˊ"; // FIXME: this method name is for Ingress v1.99.1 - v1.108.1 and maybe future versions

	public static boolean glyphSpeedTriggered = false;

	public static void debugLog(String message) {
		pref.reload();

		if (pref.getInt(DEBUGLOG, ON_OFF.OFF.ordinal()) == ON_OFF.ON.ordinal()) {
			log(TAG + "[DEBUG] " + message);
		}
	}

	@Override
	public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
		if (!lpparam.packageName.equals(INGRESSPACKAGENAME)) {
			return;
		}

		debugLog("Ingress loaded");

		pref.reload();

		if (pref.getInt(ACTIVATE, ON_OFF.OFF.ordinal()) == ON_OFF.ON.ordinal()) {
			int ingressVersion = pref.getInt(INGRESSVERSIONCODE, INGRESSVERSION20161102);

			if (ingressVersion < INGRESSVERSION20160802) {
				speedClassName = "o.mq"; // FIXME: this class name is for Ingress v1.99.1 - v1.104.1
			} else if (ingressVersion < INGRESSVERSION20161102) {
				speedClassName = "o.ms"; // FIXME: this class name is for Ingress v1.105.1 - v1.107.0
			}

			final Class<?> portalHackingParamsClass;
			final Class<?> userInputGlyphSequenceClass;
			final Class<?> glyphClass;
			final Class<?> turingClass;
			final Class<?> speedClass;

			try {
				debugLog(portalHackingParamsClassName + ": finding class");
				portalHackingParamsClass = findClass(portalHackingParamsClassName, lpparam.classLoader);
				debugLog(portalHackingParamsClassName + ": class found");
			} catch (ClassNotFoundError e) {
				debugLog(portalHackingParamsClassName + ": ClassNotFoundError");
				return;
			}

			try {
				debugLog(userInputGlyphSequenceClassName + ": finding class");
				userInputGlyphSequenceClass = findClass(userInputGlyphSequenceClassName, lpparam.classLoader);
				debugLog(userInputGlyphSequenceClassName + ": class found");
			} catch (ClassNotFoundError e) {
				debugLog(userInputGlyphSequenceClassName + ": ClassNotFoundError");
				return;
			}

			try {
				debugLog(glyphClassName + ": finding class");
				glyphClass = findClass(Xglyph.glyphClassName, lpparam.classLoader);
				debugLog(glyphClassName + ": class found");
			} catch (ClassNotFoundError e) {
				debugLog(glyphClassName + ": ClassNotFoundError");
				return;
			}

			try {
				debugLog(turingClassName + ": finding class");
				turingClass = findClass(turingClassName, lpparam.classLoader);
				debugLog(turingClassName + ": class found");
			} catch (ClassNotFoundError e) {
				debugLog(turingClassName + ": ClassNotFoundError");
				return;
			}

			try {
				debugLog(speedClassName + ": finding class");
				speedClass = findClass(speedClassName, lpparam.classLoader);
				debugLog(speedClassName + ": class found");
			} catch (ClassNotFoundError e) {
				debugLog(speedClassName + ": ClassNotFoundError");
				return;
			}

			try {
				debugLog(portalHackingParamsClassName + ": hooking constructor for normal hack");
				findAndHookConstructor(portalHackingParamsClass, String.class, boolean.class, boolean.class, new NormalHackHook());
				debugLog(portalHackingParamsClassName + ": constructor for normal hack hooked");
			} catch (NoSuchMethodError e) {
				debugLog(portalHackingParamsClassName + ": constructor for normal hack not found");
			}

			try {
				debugLog(portalHackingParamsClassName + ": hooking constructor for glyph hack");
				findAndHookConstructor(portalHackingParamsClass, String.class, userInputGlyphSequenceClass, userInputGlyphSequenceClass, new GlyphHackHook(glyphClass, userInputGlyphSequenceClass));
				debugLog(portalHackingParamsClassName + ": constructor for glyph hack hooked");
			} catch (NoSuchMethodError e) {
				debugLog(portalHackingParamsClassName + ": constructor for glyph hack not found");
			}

			try {
				debugLog(turingClassName + "." + turingClassMethodName1 + ": hooking method");
				findAndHookMethod(turingClass, turingClassMethodName1, String.class, new TranslateGlyphsHook());
				debugLog(turingClassName + "." + turingClassMethodName1 + ": method hooked");
			} catch (NoSuchMethodError error) {
				debugLog(turingClassName + "." + turingClassMethodName1 + ": NoSuchMethodError");
			}

			try {
				debugLog(turingClassName + "." + turingClassMethodName2 + ": hooking method");
				findAndHookMethod(turingClass, turingClassMethodName2, new ClearGlyphsHook());
				debugLog(turingClassName + "." + turingClassMethodName2 + ": method hooked");
			} catch (NoSuchMethodError error) {
				debugLog(turingClassName + "." + turingClassMethodName2 + ": NoSuchMethodError");
			}

			try {
				debugLog(speedClassName + "." + speedClassMethodName + ": hooking method");
				findAndHookMethod(speedClass, speedClassMethodName, String.class, new GlyphSpeedHook());
				debugLog(speedClassName + "." + speedClassMethodName + ": method hooked");
			} catch (NoSuchMethodError error) {
				debugLog(speedClassName + "." + speedClassMethodName + ": NoSuchMethodError");
			}
		} else {
			debugLog("Xglyph switched off");
		}

// ============================== HideX ==============================

		final String apmClassName = "android.app.ApplicationPackageManager";
		final Class<?> apmClass;

		try {
			apmClass = findClass(apmClassName, lpparam.classLoader);
		} catch (ClassNotFoundError e) {
			debugLog(apmClassName + ": ClassNotFoundError");
			return;
		}

		try {
			findAndHookMethod(apmClass, "getInstalledApplications", int.class, new InstalledApplicationsHook());
		} catch (NoSuchMethodError error) {
			debugLog("ApplicationPackageManager.getInstalledApplications: NoSuchMethodError");
		}

		try {
			findAndHookMethod(apmClass, "getInstalledPackages", int.class, new InstalledPackagesHook());
		} catch (NoSuchMethodError error) {
			debugLog("ApplicationPackageManager.getInstalledPackages: NoSuchMethodError");
		}
	}
}
