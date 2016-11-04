package com.cypher.xglyph2.hooks;

import com.cypher.xglyph2.GlyphTranslator;
import de.robv.android.xposed.XC_MethodHook;

import static com.cypher.xglyph2.Xglyph.*;

public class TranslateGlyphsHook extends XC_MethodHook {
	@Override
	protected void afterHookedMethod(MethodHookParam param) throws Throwable {
		debugLog(turingClassName + "." + turingClassMethodName1 + " called");

		debugLog("parameter (param[0]): " + param.args[0]);

		float[] result = (float[]) param.getResult();
		String glyph = GlyphTranslator.translate(result);

		debugLog("glyph: " + glyph);

		GlyphTranslator.sequence.add(glyph);

		debugLog("sequence.size(): " + GlyphTranslator.sequence.size());
	}
}
