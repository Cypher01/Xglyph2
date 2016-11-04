package com.cypher.xglyph2.hooks;

import com.cypher.xglyph2.GlyphTranslator;
import de.robv.android.xposed.XC_MethodHook;

import static com.cypher.xglyph2.Xglyph.*;

public class ClearGlyphsHook extends XC_MethodHook {
	@Override
	protected void afterHookedMethod(MethodHookParam param) throws Throwable {
		debugLog(turingClassName + "." + turingClassMethodName2 + " called");

		GlyphTranslator.sequence.clear();
	}
}
