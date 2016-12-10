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
	public static final String normalSpeedTrigger = "a"; // TODO: make this configurable

	public static final String portalHackingParamsClassName = "com.nianticproject.ingress.shared.rpc.PortalHackingParams";
	public static final String userInputGlyphSequenceClassName = "com.nianticproject.ingress.glyph.UserInputGlyphSequence";
	public static final String glyphClassName = "com.nianticproject.ingress.glyph.Glyph";
	public static final String turingClassName = "com.nianticproject.ingress.common.utility.Turing";
	public static final String turingClassMethodName1 = "g";
	public static final String turingClassMethodName2 = "l";
	public static String speedClassName = "o.nb"; // FIXME: this class name is for Ingress v1.108.1 and maybe future versions
	public static String speedClassMethodName = "ˊ"; // FIXME: this method name is for Ingress v1.99.1 - v1.108.1 and maybe future versions

	public static final String apmClassName = "android.app.ApplicationPackageManager";
	public static final String apmClassMethodName1 = "getInstalledApplications";
	public static final String apmClassMethodName2 = "getInstalledPackages";

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

		if (pref.getInt(ACTIVATE, ON_OFF.ON.ordinal()) == ON_OFF.ON.ordinal()) {
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
				portalHackingParamsClass = findClass(portalHackingParamsClassName, lpparam.classLoader);
			} catch (ClassNotFoundError e) {
				log(TAG + portalHackingParamsClassName + ": class not found");
				return;
			}

			try {
				userInputGlyphSequenceClass = findClass(userInputGlyphSequenceClassName, lpparam.classLoader);
			} catch (ClassNotFoundError e) {
				log(TAG + userInputGlyphSequenceClassName + ": class not found");
				return;
			}

			try {
				glyphClass = findClass(Xglyph.glyphClassName, lpparam.classLoader);
			} catch (ClassNotFoundError e) {
				log(TAG + glyphClassName + ": class not found");
				return;
			}

			try {
				turingClass = findClass(turingClassName, lpparam.classLoader);
			} catch (ClassNotFoundError e) {
				log(TAG + turingClassName + ": class not found");
				return;
			}

			try {
				speedClass = findClass(speedClassName, lpparam.classLoader);
			} catch (ClassNotFoundError e) {
				log(TAG + speedClassName + ": class not found");
				return;
			}

			try {
				findAndHookConstructor(portalHackingParamsClass, String.class, boolean.class, boolean.class, new NormalHackHook());
			} catch (NoSuchMethodError e) {
				log(TAG + portalHackingParamsClassName + ": constructor for normal hack not found");
			}

			try {
				findAndHookConstructor(portalHackingParamsClass, String.class, userInputGlyphSequenceClass, userInputGlyphSequenceClass, new GlyphHackHook(glyphClass, userInputGlyphSequenceClass));
			} catch (NoSuchMethodError e) {
				log(TAG + portalHackingParamsClassName + ": constructor for glyph hack not found");
			}

			try {
				findAndHookMethod(turingClass, turingClassMethodName1, String.class, new TranslateGlyphsHook());
			} catch (NoSuchMethodError error) {
				log(TAG + turingClassName + "." + turingClassMethodName1 + ": method not found");
			}

			try {
				findAndHookMethod(turingClass, turingClassMethodName2, new ClearGlyphsHook());
			} catch (NoSuchMethodError error) {
				log(TAG + turingClassName + "." + turingClassMethodName2 + ": method not found");
			}

			try {
				findAndHookMethod(speedClass, speedClassMethodName, String.class, new GlyphSpeedHook());
			} catch (NoSuchMethodError error) {
				log(TAG + speedClassName + "." + speedClassMethodName + ": method not found");
			}
		} else {
			debugLog("Xglyph switched off");
		}

// ============================== HideX ==============================

		final Class<?> apmClass;

		try {
			apmClass = findClass(apmClassName, lpparam.classLoader);
		} catch (ClassNotFoundError e) {
			log(TAG + apmClassName + ": class not found");
			return;
		}

		try {
			findAndHookMethod(apmClass, apmClassMethodName1, int.class, new InstalledApplicationsHook());
		} catch (NoSuchMethodError error) {
			log(TAG + apmClassName + "." + apmClassMethodName1 + ": method not found");
		}

		try {
			findAndHookMethod(apmClass, apmClassMethodName2, int.class, new InstalledPackagesHook());
		} catch (NoSuchMethodError error) {
			log(TAG + apmClassName + "." + apmClassMethodName2 + ": method not found");
		}
	}
}
