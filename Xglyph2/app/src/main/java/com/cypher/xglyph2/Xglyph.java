package com.cypher.xglyph2;

import android.os.Environment;
import android.util.Log;
import com.cypher.xglyph2.hooks.*;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.cypher.xglyph2.MainActivity.*;
import static de.robv.android.xposed.XposedBridge.*;
import static de.robv.android.xposed.XposedHelpers.*;
import static de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class Xglyph implements IXposedHookLoadPackage {
	public static final String TAG = "Xglyph²";

	public static final XSharedPreferences pref = new XSharedPreferences(Xglyph.class.getPackage().getName(), PREF);
	private static final String logfileFolder = "Xglyph2";
	private static String logfile = "";

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
	public static String speedClassName = "o.ne"; // FIXME: this class name is for Ingress v1.127.0 and maybe future versions
	public static String speedClassMethodName = "ˊ"; // FIXME: this method name is for Ingress v1.99.1 - v1.127.0 and maybe future versions

	public static String nemesisClassName = "com.nianticproject.ingress.NemesisApplication";
    public static String nemesisMethodName = "onCreate";

	public static final String apmClassName = "android.app.ApplicationPackageManager";
	public static final String apmClassMethodName1 = "getInstalledApplications";
	public static final String apmClassMethodName2 = "getInstalledPackages";

	public static boolean glyphSpeedTriggered = false;

	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		// ================================================================================
		// ==== Xglyph² (self hooking, check if module is activated) ======================
		// ================================================================================
		if (lpparam.packageName.equals(BuildConfig.APPLICATION_ID)) {
			try {
				findAndHookMethod(BuildConfig.APPLICATION_ID + ".MainActivity", lpparam.classLoader, "isXposedEnabled", new XC_MethodHook() {
					@Override
					protected void afterHookedMethod(MethodHookParam param) throws Throwable {
						param.setResult(true);
					}
				});
			} catch (ClassNotFoundError | NoSuchMethodError ignored) { }

			return;
		}

		// ================================================================================
		// ==== Ingress ===================================================================
		// ================================================================================
		if (!lpparam.packageName.equals(INGRESSPACKAGENAME) && !lpparam.packageName.equals(SCANNERREDACTEDPACKAGENAME)) {
			return;
		}

		GlyphTranslator.sequence.clear();

		if (logfile.equals("")) {
			logfile = "Xglyph2_" + getDateAndTime(true) + ".txt";
		}

		pref.reload();

		log(TAG, "Ingress loaded");

		if (pref.getInt(ACTIVATE, ACTIVATE_DEFAULT) == ON_OFF.ON.ordinal()) {
			final Class<?> portalHackingParamsClass;
			final Class<?> userInputGlyphSequenceClass;
			final Class<?> glyphClass;
			//final Class<?> turingClass;
			final Class<?> speedClass;
			final Class<?> nemesisClass;

			try {
				portalHackingParamsClass = findClass(portalHackingParamsClassName, lpparam.classLoader);
			} catch (ClassNotFoundError e) {
				log(TAG, portalHackingParamsClassName + ": class not found", true);
				return;
			}

			try {
				userInputGlyphSequenceClass = findClass(userInputGlyphSequenceClassName, lpparam.classLoader);
			} catch (ClassNotFoundError e) {
				log(TAG, userInputGlyphSequenceClassName + ": class not found", true);
				return;
			}

			try {
				glyphClass = findClass(glyphClassName, lpparam.classLoader);
			} catch (ClassNotFoundError e) {
				log(TAG, glyphClassName + ": class not found", true);
				return;
			}

			/* try {
				turingClass = findClass(turingClassName, lpparam.classLoader);
			} catch (ClassNotFoundError e) {
				log(TAG, turingClassName + ": class not found", true);
				return;
			} */

			try {
				nemesisClass = findClass(nemesisClassName, lpparam.classLoader);
			} catch (ClassNotFoundError e) {
				log(TAG, nemesisClassName + ": class not found", true);
				return;
			}

			try {
				speedClass = findClass(speedClassName, lpparam.classLoader);
			} catch (ClassNotFoundError e) {
				log(TAG, speedClassName + ": class not found", true);
				return;
			}

			try {
				findAndHookConstructor(portalHackingParamsClass, String.class, boolean.class, boolean.class, new NormalHackHook());
			} catch (NoSuchMethodError e) {
				log(TAG, portalHackingParamsClassName + ": constructor for normal hack not found", true);
			}

			try {
				findAndHookConstructor(portalHackingParamsClass, String.class, userInputGlyphSequenceClass, userInputGlyphSequenceClass, new GlyphHackHook(glyphClass, userInputGlyphSequenceClass));
			} catch (NoSuchMethodError e) {
				log(TAG, portalHackingParamsClassName + ": constructor for glyph hack not found", true);
			}

			/* try {
				findAndHookMethod(turingClass, turingClassMethodName1, String.class, new TranslateGlyphsHook());
			} catch (NoSuchMethodError error) {
				log(TAG, turingClassName + "." + turingClassMethodName1 + ": method not found", true);
			}

			try {
				findAndHookMethod(turingClass, turingClassMethodName2, new ClearGlyphsHook());
			} catch (NoSuchMethodError error) {
				log(TAG, turingClassName + "." + turingClassMethodName2 + ": method not found", true);
			} */

            try {
                findAndHookMethod(nemesisClass, nemesisMethodName, new NemesisHook(lpparam));
            } catch (NoSuchMethodError error) {
                log(TAG, nemesisClassName + "." + nemesisMethodName + ": method not found", true);
            }


			try {
				findAndHookMethod(speedClass, speedClassMethodName, String.class, new GlyphSpeedHook());
			} catch (NoSuchMethodError error) {
				log(TAG, speedClassName + "." + speedClassMethodName + ": method not found", true);
			}
		} else {
			log(TAG, "Xglyph² switched off", true);
		}

		// ================================================================================
		// ==== Xglyph² (self hiding, prevent Ingress from detecting the module) ==========
		// ================================================================================
		final Class<?> apmClass;

		try {
			apmClass = findClass(apmClassName, lpparam.classLoader);
		} catch (ClassNotFoundError e) {
			log(TAG, apmClassName + ": class not found", true);
			return;
		}

		try {
			findAndHookMethod(apmClass, apmClassMethodName1, int.class, new InstalledApplicationsHook());
		} catch (NoSuchMethodError error) {
			log(TAG, apmClassName + "." + apmClassMethodName1 + ": method not found", true);
		}

		try {
			findAndHookMethod(apmClass, apmClassMethodName2, int.class, new InstalledPackagesHook());
		} catch (NoSuchMethodError error) {
			log(TAG, apmClassName + "." + apmClassMethodName2 + ": method not found", true);
		}
	}

	public static void log(String TAG, String text) {
		log(TAG, text, false);
	}

	public static void log(String TAG, String text, boolean forceLog) {
		pref.reload();

		if (pref.getInt(DEBUGLOG, DEBUGLOG_DEFAULT) == ON_OFF.ON.ordinal() || forceLog) {
			de.robv.android.xposed.XposedBridge.log(TAG + ": " + text);
		}

		if (pref.getInt(LOGFILE, LOGFILE_DEFAULT) == ON_OFF.ON.ordinal()) {
			try {
				writeToFile(logfileFolder, logfile, text);
			} catch (IOException e) {
				de.robv.android.xposed.XposedBridge.log(TAG + ": Write log file FAILED");
				Log.e(TAG, "Write log file FAILED");
			}
		}
	}

	private static synchronized void writeToFile(String foldername, String filename, String content) throws IOException {
		File storageFile = getOrCreateFile(foldername, filename);
		FileWriter fileWriter = new FileWriter(storageFile, true);
		fileWriter.write(getDateAndTime(false) + ": " + content + "\n");
		fileWriter.flush();
		fileWriter.close();
	}

	private static synchronized File getOrCreateFile(String foldername, String filename) throws IOException {
		File file = new File(Environment.getExternalStorageDirectory(), foldername);

		if (!file.exists()) {
			if (!file.mkdir()) {
				throw new IOException("Couldn't create folder " + file.getAbsolutePath());
			}
		} else if (file.isFile()) {
			throw new IOException("File with considered foldername " + file.getAbsolutePath() + " exists");
		}

		file = new File(file, filename);

		if (!file.exists()) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
			String buildDate = dateFormat.format(new java.util.Date(BuildConfig.TIMESTAMP));

			String content = "Xglyph² v" + BuildConfig.VERSION_NAME + ", " + buildDate + "\n\n";

			FileWriter fileWriter = new FileWriter(file, true);
			fileWriter.write(content);
			fileWriter.flush();
			fileWriter.close();
		}

		return file;
	}

	private static String getDateAndTime(boolean filename) {
		Date now = new Date();

		SimpleDateFormat dateFormat;

		if (filename) {
			dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US);
		} else {
			dateFormat = new SimpleDateFormat("yyyy-MM-dd, HH:mm:ss", Locale.US);
		}

		return dateFormat.format(now);
	}
}
