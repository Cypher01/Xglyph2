package com.cypher.xglyph2.hooks;

import com.cypher.xglyph2.GlyphTranslator;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.cypher.xglyph2.MainActivity.*;
import static com.cypher.xglyph2.Xglyph.*;
import static de.robv.android.xposed.XposedHelpers.*;

public class GlyphHackHook extends XC_MethodHook {
	private final Class<?> glyphClass;
	private final Class<?> userInputGlyphSequenceClass;

	public GlyphHackHook(Class<?> glyphClass, Class<?> userInputGlyphSequenceClass) throws XposedHelpers.ClassNotFoundError {
		this.glyphClass = glyphClass;
		this.userInputGlyphSequenceClass = userInputGlyphSequenceClass;
	}

	@Override
	protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
		debugLog(portalHackingParamsClassName.substring(portalHackingParamsClassName.lastIndexOf(".")) + ": constructor with (String, UserInputGlyphSequence, UserInputGlyphSequence) called");

		pref.reload();
		int correctGlyphs = pref.getInt(CORRECTGLYPHS, ON_OFF.OFF.ordinal());
		int glyphKey = pref.getInt(GLYPHKEY, KEY.OFF.ordinal());
		int glyphSpeed = pref.getInt(GLYPHSPEED, SPEED.OFF.ordinal());

		String uigs1 = "null";
		String uigs2 = "null";

		if (param.args[1] != null) {
			uigs1 = param.args[1].toString();

			debugLog("original uigs1: " + uigs1);

			if (correctGlyphs == ON_OFF.ON.ordinal()) {
				List<Object> glyphList = new ArrayList<>();

				for (int i = 0; i < GlyphTranslator.sequence.size(); i++) {
					glyphList.add(newInstance(glyphClass, GlyphTranslator.sequence.get(i)));
				}

				long inputTimeMs = getLongField(param.args[1], "inputTimeMs");

				debugLog("inputTimeMs = " + inputTimeMs);

				Object uigs = newInstance(userInputGlyphSequenceClass, glyphList, false, inputTimeMs);
				uigs1 = uigs.toString();

				param.args[1] = uigs;
			}
		} else {
			debugLog("original uigs1: " + uigs1);
		}

		Object commandGlyphKey = null;
		Object commandGlyphSpeed = null;

		if (param.args[2] != null) {
			uigs2 = param.args[2].toString();

			debugLog("original uigs2: " + uigs2);

			List<String> glyphStringList = filterGlyphStrings(uigs2);

			debugLog("command glyph inputs: " + glyphStringList);

			if (glyphStringList.contains(moreGlyph1) || glyphStringList.contains(moreGlyph2)) {
				commandGlyphKey = newInstance(glyphClass, moreGlyph1);
			} else if (glyphStringList.contains(lessGlyph1) || glyphStringList.contains(lessGlyph2)) {
				commandGlyphKey = newInstance(glyphClass, lessGlyph1);
			}

			if (glyphStringList.contains(complexGlyph1) || glyphStringList.contains(complexGlyph2)) {
				commandGlyphSpeed = newInstance(glyphClass, complexGlyph1);
			} else if (glyphStringList.contains(simpleGlyph1) || glyphStringList.contains(simpleGlyph2)) {
				commandGlyphSpeed = newInstance(glyphClass, simpleGlyph1);
			}
		} else {
			debugLog("original uigs2: " + uigs2);
		}

		if (commandGlyphKey == null) {
			if (glyphKey == KEY.KEY.ordinal()) {
				commandGlyphKey = newInstance(glyphClass, moreGlyph1);
				debugLog("Glyph Hack key request set");
			} else if (glyphKey == KEY.NOKEY.ordinal()) {
				commandGlyphKey = newInstance(glyphClass, lessGlyph1);
				debugLog("Glyph Hack no key request set");
			}
		}

		if (commandGlyphSpeed == null) {
			if (glyphSpeed == SPEED.FAST.ordinal()) {
				commandGlyphSpeed = newInstance(glyphClass, complexGlyph1);
				debugLog("Glyph Hack fast set");
			} else if (glyphSpeed == SPEED.SLOW.ordinal()) {
				commandGlyphSpeed = newInstance(glyphClass, simpleGlyph1);
				debugLog("Glyph Hack slow set");
			}
		}

		if (commandGlyphKey != null || commandGlyphSpeed != null) {
			List<Object> glyphList = new ArrayList<>();

			int min = 0;
			int max = 0;

			if (commandGlyphKey != null) {
				glyphList.add(commandGlyphKey);

				min += 400; // average input time for more/less
				max += 500;
			}

			if (commandGlyphSpeed != null && glyphSpeedTriggered) {
				glyphList.add(commandGlyphSpeed);

				if (min != 0) {
					min += 300; // average time gap between two glyphs
					max += 300;
				}

				min += 600; // average input time for complex (simple takes less, but who cares)
				max += 800;

				glyphSpeedTriggered = false;
			}

			Random rand = new Random();
			long randomNum = (long) rand.nextInt((max - min) + 1) + min;

			Object uigs = newInstance(userInputGlyphSequenceClass, glyphList, false, randomNum);
			uigs2 = uigs.toString();

			param.args[2] = uigs;
		}

		debugLog("patched uigs1: " + uigs1);
		debugLog("patched uigs2: " + uigs2);
	}

	private List<String> filterGlyphStrings(String sequence) {
		List<String> glyphStringList = new ArrayList<>();

		int glyphStart;
		int glyphStop = 0;
		String glyphString;

		while (!sequence.substring(glyphStop, glyphStop + 2).equals("}]")) {
			glyphStart = sequence.substring(glyphStop).indexOf("glyphOrder=") + 11 + glyphStop;
			glyphStop = sequence.substring(glyphStart).indexOf("}") + glyphStart;

			glyphString = sequence.substring(glyphStart, glyphStop);

			glyphStringList.add(glyphString);
		}

		return glyphStringList;
	}
}
