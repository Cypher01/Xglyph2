package com.cypher.xglyph2;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Based on source code by xfunforx
 */
public class Translator {
	public static List<String> sequence = new ArrayList<>();

	private static Double[] pub = {
			 0.0,  1.0, // a
			 0.8,  0.4, // b
			 0.8, -0.5, // c
			 0.0, -1.0, // d
			-0.8, -0.5, // e
			-0.8,  0.4, // f
			-0.4,  0.2, // g
			 0.4,  0.2, // h
			 0.4, -0.2, // i
			-0.4, -0.2, // j
			 0.0,  0.0  // k
	};

	public static String translate(float[] af) {
		StringBuilder result = new StringBuilder();

		for (int i = 0; i < af.length - 1; i += 2) {
			for (int j = 0; j < pub.length - 1; j += 2) {
				double a = getDouble(af[i]);
				double b = getDouble(af[i + 1]);

				if (Math.abs(pub[j] - a) < 0.1 && Math.abs(pub[j + 1] - b) < 0.1) {
					switch (j) {
						case 0:
							result.append("a");
							break;
						case 2:
							result.append("b");
							break;
						case 4:
							result.append("c");
							break;
						case 6:
							result.append("d");
							break;
						case 8:
							result.append("e");
							break;
						case 10:
							result.append("f");
							break;
						case 12:
							result.append("g");
							break;
						case 14:
							result.append("h");
							break;
						case 16:
							result.append("i");
							break;
						case 18:
							result.append("j");
							break;
						case 20:
							result.append("k");
							break;
					}
				}
			}
		}

		StringBuilder real = new StringBuilder();
		String tmp = result.toString();
		real.append(tmp.charAt(0));

		for (int i = 1; i < tmp.length(); i++) {
			if (real.charAt(real.length() - 1) != tmp.charAt(i)) {
				real.append(tmp.charAt(i));
			}
		}

		Random random = new Random(System.currentTimeMillis());
		int r = random.nextInt(10);
		String converted;

		if (r < 5) {
			converted = new StringBuilder(real.toString()).reverse().toString();
		} else {
			converted = real.toString();
		}

		String begin = converted.substring(0, 1);
		String end = converted.substring(converted.length() - 1, converted.length());

		if (begin.equals(end)) {
			int m = random.nextInt(converted.length());
			String a = converted.substring(0, m);
			String b = converted.substring(m, converted.length() - 1);
			converted = b + a + converted.substring(m, m + 1);
		}

		// correct "imperfect"
		if (converted.equals("khkjkgj")) {
			converted = "kgjkhj";
		} else if (converted.equals("jgkjkhk")) {
			converted = "jhkjgk";
		}

		return converted;
	}

	private static double getDouble(float a) {
		return (int) (a * 100) / 100.0;
	}
}
