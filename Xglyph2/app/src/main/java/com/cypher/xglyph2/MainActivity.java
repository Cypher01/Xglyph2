package com.cypher.xglyph2;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class MainActivity extends Activity {
	private static final String TAG = "Xglyph²";

	public static final String INGRESSPACKAGENAME = "com.nianticproject.ingress";
	public static final int INGRESSVERSION20160802 = 11051;

	public static final String PREF = TAG + "_Pref";
	public static final String ACTIVATE = TAG + "_Activate";
	public static final String CORRECTGLYPHS = TAG + "_CorrectGlyphs";
	public static final String GLYPHKEY = TAG + "_GlyphKey";
	public static final String GLYPHSPEED = TAG + "_GlyphSpeed";
	public static final String NORMALHACKKEY = TAG + "_NormalHackKey";
	public static final String SOUND = TAG + "_Sound";
	public static final String DEBUGLOG = TAG + "_DebugLog";
	public static final String INGRESSVERSIONCODE = TAG + "_IngressVersionCode";

	public enum ON_OFF {OFF, ON}
	public enum KEY {OFF, KEY, NOKEY}
	public enum SPEED {OFF, FAST, SLOW}

	private ON_OFF activate;
	private ON_OFF correctGlyphs;
	private KEY glyphKey;
	private SPEED glyphSpeed;
	private KEY normalHack;
	private ON_OFF sound;
	private ON_OFF debugLog;

	private boolean showToast;

	private SharedPreferences pref;
	private Resources res;

	private LinearLayout description_module;
	private LinearLayout description_glyphGame;
	private LinearLayout description_commandChannel;
	private LinearLayout description_normalHack;
	private LinearLayout description_misc;
	private LinearLayout description_credits;
	private LinearLayout description_donation;
	private Button button_activate;
	private Button button_correctGlyphs;
	private Button button_glyphKey;
	private Button button_glyphSpeed;
	private Button button_normalHack;
	private Button button_sound;
	private Button button_debugLog;
	private Button button_donate;
	private MediaPlayer mp_startSound;
	private MediaPlayer mp_buttonSound;
	private MediaPlayer mp_exitSound;

	private String[] activateText;
	private String[] correctGlyphsText;
	private String[] glyphKeyText;
	private String[] glyphSpeedText;
	private String[] normalHackText;
	private String[] soundText;
	private String[] debugLogText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		showToast = false;

		pref = getSharedPreferences(PREF, MODE_WORLD_READABLE);

		description_module = (LinearLayout) findViewById(R.id.description_module);
		description_glyphGame = (LinearLayout) findViewById(R.id.description_glyphGame);
		description_commandChannel = (LinearLayout) findViewById(R.id.description_commandChannel);
		description_normalHack = (LinearLayout) findViewById(R.id.description_normalHack);
		description_misc = (LinearLayout) findViewById(R.id.description_misc);
		description_credits = (LinearLayout) findViewById(R.id.description_credits);
		description_donation = (LinearLayout) findViewById(R.id.description_donation);
		button_activate = (Button) findViewById(R.id.button_activate);
		button_correctGlyphs = (Button) findViewById(R.id.button_correctGlyphs);
		button_glyphKey = (Button) findViewById(R.id.button_glyphKey);
		button_glyphSpeed = (Button) findViewById(R.id.button_glyphSpeed);
		button_normalHack = (Button) findViewById(R.id.button_normalHack);
		button_sound = (Button) findViewById(R.id.button_sound);
		button_debugLog = (Button) findViewById(R.id.button_debugLog);
		button_donate = (Button) findViewById(R.id.button_donate);

		mp_startSound = MediaPlayer.create(this, R.raw.sfx_glyphgame_score_positive);
		mp_buttonSound = MediaPlayer.create(this, R.raw.sfx_ui_success);
		mp_exitSound =  MediaPlayer.create(this, R.raw.sfx_ui_back);

		res = getResources();

		activateText = new String[] {
				res.getString(R.string.activate_off),
				res.getString(R.string.activate_on)
		};

		correctGlyphsText = new String[] {
				res.getString(R.string.correct_glyphs_off),
				res.getString(R.string.correct_glyphs_on)
		};

		glyphKeyText = new String[] {
				res.getString(R.string.glyph_key_off),
				res.getString(R.string.glyph_key_key),
				res.getString(R.string.glyph_key_no_key)
		};

		glyphSpeedText = new String[] {
				res.getString(R.string.glyph_speed_off),
				res.getString(R.string.glyph_speed_fast),
				res.getString(R.string.glyph_speed_slow)
		};

		normalHackText = new String[] {
				res.getString(R.string.normal_hack_off),
				res.getString(R.string.normal_hack_key),
				res.getString(R.string.normal_hack_no_key)
		};

		soundText = new String[] {
				res.getString(R.string.sound_off),
				res.getString(R.string.sound_on)
		};

		debugLogText = new String[] {
				res.getString(R.string.debug_log_off),
				res.getString(R.string.debug_log_on)
		};

		setListeners();

		try {
			String version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;

			ZipFile zf = new ZipFile(getPackageManager().getApplicationInfo(getPackageName(), 0).sourceDir);
			ZipEntry ze = zf.getEntry("classes.dex");
			long time = ze.getTime();
			zf.close();

			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
			String buildDate = dateFormat.format(new java.util.Date(time));

			String text = res.getString(R.string.app_name) + " " + String.format(res.getString(R.string.credits_version), version, buildDate);

			TextView tv_credits = (TextView) findViewById(R.id.tv_credits);

			if (tv_credits != null) {
				tv_credits.setText(text);
			}
		} catch (PackageManager.NameNotFoundException | IOException ignored) { }

		try {
			PackageInfo ingress = findIngress();
			pref.edit().putInt(INGRESSVERSIONCODE, ingress.versionCode).apply();
		} catch (IngressNotInstalledException e) {
			showDialog(
					res.getString(R.string.warning_ingress_version_header),
					res.getString(R.string.warning_ingress_version)
			);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		showToast = false;

		int prefActivate = pref.getInt(ACTIVATE, ON_OFF.OFF.ordinal());
		int prefCorrectGlyphs = pref.getInt(CORRECTGLYPHS, ON_OFF.OFF.ordinal());
		int prefGlyphKey = pref.getInt(GLYPHKEY, KEY.OFF.ordinal());
		int prefGlyphSpeed = pref.getInt(GLYPHSPEED, SPEED.OFF.ordinal());
		int prefNormalHack = pref.getInt(NORMALHACKKEY, KEY.OFF.ordinal());
		int prefSound = pref.getInt(SOUND, ON_OFF.ON.ordinal());
		int prefDebugLog = pref.getInt(DEBUGLOG, ON_OFF.OFF.ordinal());

		activate = ON_OFF.values()[prefActivate];
		button_activate.setText(activateText[prefActivate]);

		correctGlyphs = ON_OFF.values()[prefCorrectGlyphs];
		button_correctGlyphs.setText(correctGlyphsText[prefCorrectGlyphs]);

		glyphKey = KEY.values()[prefGlyphKey];
		button_glyphKey.setText(glyphKeyText[prefGlyphKey]);

		glyphSpeed = SPEED.values()[prefGlyphSpeed];
		button_glyphSpeed.setText(glyphSpeedText[prefGlyphSpeed]);

		normalHack = KEY.values()[prefNormalHack];
		button_normalHack.setText(normalHackText[prefNormalHack]);

		sound = ON_OFF.values()[prefSound];
		button_sound.setText(soundText[prefSound]);

		debugLog = ON_OFF.values()[prefDebugLog];
		button_debugLog.setText(debugLogText[prefDebugLog]);

		showToast = true;

		startSound();
	}

	@Override
	protected void onPause() {
		super.onPause();

		showToast = false;

		pref.edit().putInt(ACTIVATE, activate.ordinal()).apply();
		pref.edit().putInt(CORRECTGLYPHS, correctGlyphs.ordinal()).apply();
		pref.edit().putInt(NORMALHACKKEY, normalHack.ordinal()).apply();
		pref.edit().putInt(GLYPHKEY, glyphKey.ordinal()).apply();
		pref.edit().putInt(GLYPHSPEED, glyphSpeed.ordinal()).apply();
		pref.edit().putInt(SOUND, sound.ordinal()).apply();
		pref.edit().putInt(DEBUGLOG, debugLog.ordinal()).apply();

		exitSound();

		showToast = true;
	}

	private void setListeners() {
		button_activate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				activate = ON_OFF.values()[(activate.ordinal() + 1) % ON_OFF.values().length];
				button_activate.setText(activateText[activate.ordinal()]);

				buttonSound();

				if (showToast) {
					Toast.makeText(getApplicationContext(), "Ingress restart required", Toast.LENGTH_SHORT).show();
				}
			}
		});

		button_correctGlyphs.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				correctGlyphs = ON_OFF.values()[(correctGlyphs.ordinal() + 1) % ON_OFF.values().length];
				button_correctGlyphs.setText(correctGlyphsText[correctGlyphs.ordinal()]);

				buttonSound();
			}
		});

		button_glyphKey.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				glyphKey = KEY.values()[(glyphKey.ordinal() + 1) % KEY.values().length];
				button_glyphKey.setText(glyphKeyText[glyphKey.ordinal()]);

				buttonSound();
			}
		});

		button_glyphSpeed.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				glyphSpeed = SPEED.values()[(glyphSpeed.ordinal() + 1) % SPEED.values().length];
				button_glyphSpeed.setText(glyphSpeedText[glyphSpeed.ordinal()]);

				buttonSound();
			}
		});

		button_normalHack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				normalHack = KEY.values()[(normalHack.ordinal() + 1) % KEY.values().length];
				button_normalHack.setText(normalHackText[normalHack.ordinal()]);

				buttonSound();
			}
		});

		button_sound.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				sound = ON_OFF.values()[(sound.ordinal() + 1) % ON_OFF.values().length];
				button_sound.setText(soundText[sound.ordinal()]);

				buttonSound();
			}
		});

		button_debugLog.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				debugLog = ON_OFF.values()[(debugLog.ordinal() + 1) % ON_OFF.values().length];
				button_debugLog.setText(debugLogText[debugLog.ordinal()]);

				buttonSound();
			}
		});

		button_donate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				buttonSound();

				try {
					Uri uri = Uri.parse("bitcoin:" + R.string.bitcoin_fingerprint);
					Intent intent = new Intent(Intent.ACTION_VIEW, uri);
					startActivity(intent);
				} catch (ActivityNotFoundException e) {
/*					showDialog(
							res.getString(R.string.warning_bitcoin_header),
							res.getString(R.string.warning_bitcoin)
					);
*/
					Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=com.mycelium.wallet");
					Intent intent = new Intent(Intent.ACTION_VIEW, uri);
					startActivity(intent);
				}
			}
		});

		description_module.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				showDialog(
						res.getString(R.string.description_module_header),
						res.getString(R.string.description_module)
				);
			}
		});

		description_glyphGame.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				showDialog(
						res.getString(R.string.description_glyph_game_header),
						res.getString(R.string.description_glyph_game)
				);
			}
		});

		description_commandChannel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				showDialog(
						res.getString(R.string.description_command_channel_key_header),
						res.getString(R.string.description_command_channel_key),
						res.getString(R.string.description_command_channel_nokey_header),
						res.getString(R.string.description_command_channel_nokey),
						res.getString(R.string.description_command_channel_fast_header),
						res.getString(R.string.description_command_channel_fast),
						res.getString(R.string.description_command_channel_slow_header),
						res.getString(R.string.description_command_channel_slow),
						res.getString(R.string.description_command_channel_override_header),
						res.getString(R.string.description_command_channel_override)
				);
			}
		});

		description_normalHack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				showDialog(
						res.getString(R.string.description_normal_hack_key_header),
						res.getString(R.string.description_normal_hack_key),
						res.getString(R.string.description_normal_hack_nokey_header),
						res.getString(R.string.description_normal_hack_nokey)
				);
			}
		});

		description_misc.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				showDialog(
						res.getString(R.string.description_misc_sound_header),
						res.getString(R.string.description_misc_sound),
						res.getString(R.string.description_misc_debug_log_header),
						res.getString(R.string.description_misc_debug_log)
				);
			}
		});

		description_credits.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				showDialog(
						res.getString(R.string.description_credits_header),
						res.getString(R.string.description_credits)
				);
			}
		});

		description_donation.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				showDialog(
						res.getString(R.string.description_donation_header),
						res.getString(R.string.description_donation)
				);
			}
		});
	}

	private void startSound() {
		if (sound == ON_OFF.ON) {
			mp_startSound.start();
		}
	}

	private void buttonSound() {
		if (sound == ON_OFF.ON) {
			mp_buttonSound.start();
		}
	}

	private void exitSound() {
		if (sound == ON_OFF.ON) {
			mp_exitSound.start();
		}
	}

	private void showDialog(String... texts) {
		final Dialog description = new Dialog(MainActivity.this);

		description.setContentView(R.layout.dialog);
		LinearLayout ll_description = (LinearLayout) description.findViewById(R.id.ll_description);

		Button button_close = (Button) description.findViewById(R.id.button_close);
		button_close.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				description.cancel();
			}
		});

		for (int i = 1; i < texts.length; i+=2) {
			LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			LinearLayout ll_entry = (LinearLayout) inflater.inflate(R.layout.dialog_entry, null);

			TextView tv_title = (TextView) ll_entry.findViewById(R.id.tv_title);
			TextView tv_description = (TextView) ll_entry.findViewById(R.id.tv_description);

			tv_title.setText(texts[i-1]);
			tv_description.setText(texts[i]);

			ll_description.addView(ll_entry);
		}

		description.show();
	}

	private void showDescriptionSimple(String... texts) {
		final Dialog description = new Dialog(MainActivity.this);

		description.setContentView(R.layout.dialog_simple);
		LinearLayout ll_description = (LinearLayout) description.findViewById(R.id.ll_description);

		Button button_close = (Button) description.findViewById(R.id.button_close);
		button_close.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				description.cancel();
			}
		});

		for (int i = 0; i < texts.length; i++) {
			TextView textView = new TextView(description.getContext());

			if (i % 2 == 0) {
				textView.setTypeface(null, android.graphics.Typeface.BOLD);

				if (i > 0) {
					textView.setPadding(24, 24, 0, 0);
				} else {
					textView.setPadding(24, 0, 0, 0);
				}
			}

			textView.setText(texts[i]);
			ll_description.addView(textView);
		}

		description.show();
	}

	private PackageInfo findIngress() throws IngressNotInstalledException {
		List<PackageInfo> installedPackages = getPackageManager().getInstalledPackages(0);

		for (PackageInfo installedPackage : installedPackages) {
			if (installedPackage.packageName.equals(INGRESSPACKAGENAME)) {
				return installedPackage;
			}
		}

		throw new IngressNotInstalledException();
	}
}
