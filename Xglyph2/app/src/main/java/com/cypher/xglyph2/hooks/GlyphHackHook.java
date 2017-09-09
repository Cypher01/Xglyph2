package com.cypher.xglyph2.hooks;

import com.cypher.xglyph2.GlyphTranslator;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.cypher.xglyph2.MainActivity.*;
import static com.cypher.xglyph2.Xglyph.*;
import static com.cypher.xglyph2.Xglyph.TAG;
import static de.robv.android.xposed.XposedHelpers.*;

public class GlyphHackHook extends XC_MethodHook {
	private final Class<?> glyphClass;
	private final Class<?> userInputGlyphSequenceClass;
	private final Random rand;

	public GlyphHackHook(Class<?> glyphClass, Class<?> userInputGlyphSequenceClass) throws XposedHelpers.ClassNotFoundError {
		this.glyphClass = glyphClass;
		this.userInputGlyphSequenceClass = userInputGlyphSequenceClass;
		this.rand = new Random();
	}

	@Override
	protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
		log(TAG, portalHackingParamsClassName.substring(portalHackingParamsClassName.lastIndexOf(".") + 1) + ": constructor for glyph hack called");

		pref.reload();

		int correctGlyphs = pref.getInt(CORRECTGLYPHS, CORRECTGLYPHS_DEFAULT);
		int glyphKey = pref.getInt(GLYPHKEY, GLYPHKEY_DEFAULT);
		int glyphSpeed = pref.getInt(GLYPHSPEED, GLYPHSPEED_DEFAULT);
		int bypass = pref.getInt(BYPASS, BYPASS_DEFAULT);

		String uigs1 = "null";
		String uigs2 = "null";

		boolean bypassed_uigs1 = false;
		boolean bypassed_uigs2 = false;

		if (param.args[1] != null) {
			uigs1 = param.args[1].toString();

			log(TAG, "original uigs1: " + uigs1);

			bypassed_uigs1 = getBooleanField(param.args[1], "bypassed");

			log(TAG, "bypassed: " + bypassed_uigs1);

			if (!bypassed_uigs1 || bypass == ON_OFF.ON.ordinal()) {
				if (correctGlyphs == ON_OFF.ON.ordinal()) {
					List<Object> glyphList = new ArrayList<>();

					for (int i = 0; i < GlyphTranslator.sequence.size(); i++) {
						glyphList.add(newInstance(glyphClass, GlyphTranslator.sequence.get(i)));
					}

					long inputTimeMs;

					if (bypassed_uigs1 && bypass == ON_OFF.ON.ordinal()) {
						log(TAG, "bypass hooked");

						inputTimeMs = calculateInputTime();

						log(TAG, "inputTimeMs set to " + inputTimeMs);

						if (inputTimeMs == -1L) {
							// bypass has been hit too early
							return;
						}
					} else {
						inputTimeMs = getLongField(param.args[1], "inputTimeMs");

						log(TAG, "inputTimeMs = " + inputTimeMs);
					}

					Object uigs = newInstance(userInputGlyphSequenceClass, glyphList, false, inputTimeMs);
					uigs1 = uigs.toString();

					param.args[1] = uigs;
				} else {
					log(TAG, "Correct Glyphs switched off");
				}
			}
		} else {
			log(TAG, "original uigs1: " + uigs1 + " (else)");
		}

		Object commandGlyphKey = null;
		Object commandGlyphSpeed = null;

		if (param.args[2] != null) {
			uigs2 = param.args[2].toString();

			log(TAG, "original uigs2: " + uigs2);

			bypassed_uigs2 = getBooleanField(param.args[2], "bypassed");

			log(TAG, "bypassed: " + bypassed_uigs2);

			if (!bypassed_uigs2) { // this should never happen, because param.args[2] is null in case of bypassed, but let's check it for the sake of completeness
				List<String> glyphStringList = filterGlyphStrings(uigs2);

				log(TAG, "command glyph inputs: " + glyphStringList);

				if (glyphStringList.contains(moreGlyph1) || glyphStringList.contains(moreGlyph2)) {
					commandGlyphKey = newInstance(glyphClass, moreGlyph1);
					log(TAG, "Glyph Hack key request set (overridden)");
				} else if (glyphStringList.contains(lessGlyph1) || glyphStringList.contains(lessGlyph2)) {
					commandGlyphKey = newInstance(glyphClass, lessGlyph1);
					log(TAG, "Glyph Hack no key request set (overridden)");
				}

				if (glyphStringList.contains(complexGlyph1) || glyphStringList.contains(complexGlyph2)) {
					commandGlyphSpeed = newInstance(glyphClass, complexGlyph1);
					log(TAG, "Glyph Hack fast set (overridden)");
				} else if (glyphStringList.contains(simpleGlyph1) || glyphStringList.contains(simpleGlyph2)) {
					commandGlyphSpeed = newInstance(glyphClass, simpleGlyph1);
					log(TAG, "Glyph Hack slow set (overridden)");
				}
			}
		} else {
			log(TAG, "original uigs2: " + uigs2 + " (else)");
		}

		if (bypass == ON_OFF.ON.ordinal()) {
			glyphSpeedTriggered = true;
		}

		if (!bypassed_uigs1 && !bypassed_uigs2 || bypass == ON_OFF.ON.ordinal()) {
			if (commandGlyphKey == null) {
				if (glyphKey == KEY.KEY.ordinal()) {
					commandGlyphKey = newInstance(glyphClass, moreGlyph1);
					log(TAG, "Glyph Hack key request set");
				} else if (glyphKey == KEY.NOKEY.ordinal()) {
					commandGlyphKey = newInstance(glyphClass, lessGlyph1);
					log(TAG, "Glyph Hack no key request set");
				} else {
					log(TAG, "Glyph Hack key switched off");
				}
			}

			if (commandGlyphSpeed == null && glyphSpeedTriggered) {
				if (glyphSpeed == SPEED.FAST.ordinal()) {
					commandGlyphSpeed = newInstance(glyphClass, complexGlyph1);
					log(TAG, "Glyph Hack fast set");
				} else if (glyphSpeed == SPEED.SLOW.ordinal()) {
					commandGlyphSpeed = newInstance(glyphClass, simpleGlyph1);
					log(TAG, "Glyph Hack slow set");
				} else {
					log(TAG, "Glyph Hack speed switched off");
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

				long randomNum = (long) rand.nextInt((max - min) + 1) + min;

				Object uigs = newInstance(userInputGlyphSequenceClass, glyphList, false, randomNum);
				uigs2 = uigs.toString();

				param.args[2] = uigs;
			}
		}

		log(TAG, "patched uigs1: " + uigs1);
		log(TAG, "patched uigs2: " + uigs2);
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

	private long calculateInputTime() {
		double min = (double) pref.getInt(SPEEDBONUSMIN, SPEEDBONUSMIN_DEFAULT);
		double max = (double) pref.getInt(SPEEDBONUSMAX, SPEEDBONUSMAX_DEFAULT);
		int numGlyphs = GlyphTranslator.sequence.size();
		double timeLimit;

		// portal level    #glyphs    time limit
		//       1            1           20
		//       2            2           20
		//       3            3           20
		//       4            3           19
		//       5            3           18
		//       6            4           17
		//       7            4           16
		//       8            5           15

		// TODO: involve also the portal level, these values are only lower limits calculated from #glyphs
		if (numGlyphs == 0) {
			// bypass has been hit too early
			return -1L;
		} else if (numGlyphs <= 2) {
			timeLimit = 20d;
		} else if (numGlyphs == 3) {
			timeLimit = 18d;
		} else if (numGlyphs == 4) {
			timeLimit = 16d;
		} else if (numGlyphs == 5) {
			timeLimit = 15d;
		} else {
			return -1L; // this should never happen, 5 glyphs are max
		}

		double speedBonus = (max - min) * rand.nextDouble() + min;

		return (long) ((timeLimit * 1000d) - (speedBonus * timeLimit * 10d));
	}
}
