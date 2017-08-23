package com.cypher.xglyph2.hooks;

import com.cypher.xglyph2.GlyphTranslator;
import de.robv.android.xposed.XC_MethodHook;

import static com.cypher.xglyph2.Xglyph.*;

public class TranslateGlyphsHook extends XC_MethodHook {
	@Override
	protected void afterHookedMethod(MethodHookParam param) throws Throwable {
		log(TAG, turingClassName + "." + turingClassMethodName1 + " called");

		log(TAG, "parameter (param[0]): " + param.args[0]);

		float[] result = (float[]) param.getResult();
		String glyph = GlyphTranslator.translate(result);

		log(TAG, "glyph: " + glyph);

		GlyphTranslator.sequence.add(glyph);

		log(TAG, "sequence.size(): " + GlyphTranslator.sequence.size());
	}
}
