package com.cypher.xglyph2.hooks;

import de.robv.android.xposed.XC_MethodHook;

import static com.cypher.xglyph2.MainActivity.*;
import static com.cypher.xglyph2.Xglyph.*;
import static com.cypher.xglyph2.Xglyph.TAG;

public class NormalHackHook extends XC_MethodHook {
	@Override
	protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
		log(TAG, portalHackingParamsClassName.substring(portalHackingParamsClassName.lastIndexOf(".") + 1) + ": constructor for hack called");

		String arg0 = (String) param.args[0];
		boolean glyphGameRequested = (boolean) param.args[1];
		boolean hackNoKey = (boolean) param.args[2];

		log(TAG, "arg0 = " + arg0 + ", glyphGameRequested = " + glyphGameRequested + ", hackNoKey = " + hackNoKey);

		if (!glyphGameRequested) {
			pref.reload();

			int normalHackKey = pref.getInt(NORMALHACKKEY, NORMALHACKKEY_DEFAULT);

			if (normalHackKey == KEY.KEY.ordinal()) {
				param.args[2] = false;
				log(TAG, "Normal Hack key request set");
			} else if (normalHackKey == KEY.NOKEY.ordinal()) {
				param.args[2] = true;
				log(TAG, "Normal Hack no key request set");
			} else {
				log(TAG, "Normal Hack switched off");
			}
		}
	}
}
