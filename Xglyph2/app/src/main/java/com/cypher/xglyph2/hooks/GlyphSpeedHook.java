package com.cypher.xglyph2.hooks;

import de.robv.android.xposed.XC_MethodHook;

import static com.cypher.xglyph2.MainActivity.*;
import static com.cypher.xglyph2.Xglyph.*;

public class GlyphSpeedHook extends XC_MethodHook {
	@Override
	protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
		debugLog(speedClassName + "." + speedClassMethodName + " called");

		String glyphString = (String) param.args[0];

		debugLog("glyphString (param[0]): " + glyphString);

		glyphSpeedTriggered = true;

		pref.reload();

		int glyphSpeed = pref.getInt(GLYPHSPEED, SPEED.OFF.ordinal());

		if (glyphSpeed != SPEED.OFF.ordinal()) {
			String commandGlyphString = "";

			if (glyphString.equals(complexGlyph1) || glyphString.equals(complexGlyph2)) {
				commandGlyphString = complexGlyph1;
			} else if (glyphString.equals(simpleGlyph1) || glyphString.equals(simpleGlyph2)) {
				commandGlyphString = simpleGlyph1;
			}

			if (commandGlyphString.equals("")) {
				if (glyphSpeed == SPEED.FAST.ordinal()) {
					param.args[0] = complexGlyph1;
					debugLog("Glyph Hack fast set");
				} else if (glyphSpeed == SPEED.SLOW.ordinal()) {
					param.args[0] = simpleGlyph1;
					debugLog("Glyph Hack slow set");
				}
			}
		}
	}
}
