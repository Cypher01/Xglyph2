package com.cypher.xglyph2.hooks;

import de.robv.android.xposed.XC_MethodHook;

import static com.cypher.xglyph2.MainActivity.*;
import static com.cypher.xglyph2.Xglyph.*;
import static com.cypher.xglyph2.Xglyph.TAG;

public class GlyphSpeedHook extends XC_MethodHook {
	@Override
	protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
		log(TAG, speedClassName + "." + speedClassMethodName + " called");

		String glyphString = (String) param.args[0];

		log(TAG, "glyphString (param[0]): " + glyphString);

		glyphSpeedTriggered = true;

		pref.reload();

		int glyphSpeed = pref.getInt(GLYPHSPEED, SPEED.OFF.ordinal());

		if (glyphSpeed != SPEED.OFF.ordinal()) {
			String commandGlyphString = "";

			if (glyphString.equals(complexGlyph1) || glyphString.equals(complexGlyph2)) {
				commandGlyphString = complexGlyph1;
			} else if (glyphString.equals(simpleGlyph1) || glyphString.equals(simpleGlyph2)) {
				commandGlyphString = simpleGlyph1;
			} else if (glyphString.equals(normalSpeedTrigger)) {
				commandGlyphString = normalSpeedTrigger;
				glyphSpeedTriggered = false;
				log(TAG, "Glyph Hack normal speed set");
			}

			if (commandGlyphString.equals("")) {
				if (glyphSpeed == SPEED.FAST.ordinal()) {
					param.args[0] = complexGlyph1;
					log(TAG, "Glyph Hack fast set");
				} else if (glyphSpeed == SPEED.SLOW.ordinal()) {
					param.args[0] = simpleGlyph1;
					log(TAG, "Glyph Hack slow set");
				}
			}
		} else {
			log(TAG, "Glyph Hack speed switched off");
		}
	}
}
