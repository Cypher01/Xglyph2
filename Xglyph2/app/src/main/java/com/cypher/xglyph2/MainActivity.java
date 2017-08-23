package com.cypher.xglyph2;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
	public static final String TAG = "Xglyph2";

	public static final String XPOSEDINSTALLERPACKAGENAME = "de.robv.android.xposed.installer";
	public static final String INGRESSPACKAGENAME = "com.nianticproject.ingress";
	public static final int INGRESSVERSION20160802 = 11051;
	public static final int INGRESSVERSION20161102 = 11081;
	public static final int INGRESSVERSION20170821 = 11220;

	public static final String PREF = TAG + "_Pref";
	public static final String ACTIVATE = TAG + "_Activate";
	public static final String CORRECTGLYPHS = TAG + "_CorrectGlyphs";
	public static final String GLYPHKEY = TAG + "_GlyphKey";
	public static final String GLYPHSPEED = TAG + "_GlyphSpeed";
	public static final String NORMALHACKKEY = TAG + "_NormalHackKey";
	public static final String SOUND = TAG + "_Sound";
	public static final String DEBUGLOG = TAG + "_DebugLog";
	public static final String LOGFILE = TAG + "_Logfile";
	public static final String INGRESSVERSIONCODE = TAG + "_IngressVersionCode";

	public static final int ACTIVATE_DEFAULT = ON_OFF.ON.ordinal();
	public static final int CORRECTGLYPHS_DEFAULT = ON_OFF.ON.ordinal();
	public static final int GLYPHKEY_DEFAULT = KEY.OFF.ordinal();
	public static final int GLYPHSPEED_DEFAULT = SPEED.OFF.ordinal();
	public static final int NORMALHACKKEY_DEFAULT = KEY.OFF.ordinal();
	public static final int SOUND_DEFAULT = ON_OFF.ON.ordinal();
	public static final int DEBUGLOG_DEFAULT = ON_OFF.OFF.ordinal();
	public static final int LOGFILE_DEFAULT = ON_OFF.OFF.ordinal();

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
	private ON_OFF logfile;

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
	private Button button_logfile;
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
	private String[] logfileText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		showToast = false;

		try {
			pref = getSharedPreferences(PREF, MODE_WORLD_READABLE);
		} catch (Exception e) {
			pref = getSharedPreferences(PREF, MODE_PRIVATE);
		}

		res = getResources();

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
		button_logfile = (Button) findViewById(R.id.button_logfile);
		button_donate = (Button) findViewById(R.id.button_donate);

		mp_startSound = MediaPlayer.create(this, R.raw.sfx_glyphgame_score_positive);
		mp_buttonSound = MediaPlayer.create(this, R.raw.sfx_ui_success);
		mp_exitSound =  MediaPlayer.create(this, R.raw.sfx_ui_back);

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

		logfileText = new String[] {
				res.getString(R.string.logfile_off),
				res.getString(R.string.logfile_on)
		};

		setListeners();

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
		String buildDate = dateFormat.format(new java.util.Date(BuildConfig.TIMESTAMP));

		String text = res.getString(R.string.app_name) + " " + String.format(res.getString(R.string.credits_version), BuildConfig.VERSION_NAME, buildDate);

		TextView tv_credits = (TextView) findViewById(R.id.tv_credits);
		tv_credits.setText(text);

		// first, check if self hooking works
		if (!isXposedEnabled()) {
			try {
				// second, check if the Xposed Framework is installed/loaded and the Xposed Installer is installed
				if (!isXposedAvailable()) {
					throw new XposedNotInstalledException();
				}

				findPackage(XPOSEDINSTALLERPACKAGENAME);

				showDialog(
						res.getString(R.string.warning_xposed_activation_header),
						res.getString(R.string.warning_xposed_activation)
				);
			} catch (AppNotInstalledException | XposedNotInstalledException e) {
				showDialog(
						res.getString(R.string.warning_xposed_header),
						res.getString(R.string.warning_xposed)
				);
			}
		}

		// check Android version
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
			showDialog(
					res.getString(R.string.warning_android_version_header),
					res.getString(R.string.warning_android_version)
			);
		}

		// check Ingress version
		try {
			PackageInfo ingress = findPackage(INGRESSPACKAGENAME);
			pref.edit().putInt(INGRESSVERSIONCODE, ingress.versionCode).apply();
		} catch (AppNotInstalledException e) {
			showDialog(
					res.getString(R.string.warning_ingress_version_header),
					res.getString(R.string.warning_ingress_version)
			);
		}

		descriptionHint();
	}

	@Override
	protected void onResume() {
		super.onResume();

		showToast = false;

		int prefActivate = pref.getInt(ACTIVATE, ACTIVATE_DEFAULT);
		int prefCorrectGlyphs = pref.getInt(CORRECTGLYPHS, CORRECTGLYPHS_DEFAULT);
		int prefGlyphKey = pref.getInt(GLYPHKEY, GLYPHKEY_DEFAULT);
		int prefGlyphSpeed = pref.getInt(GLYPHSPEED, GLYPHSPEED_DEFAULT);
		int prefNormalHack = pref.getInt(NORMALHACKKEY, NORMALHACKKEY_DEFAULT);
		int prefSound = pref.getInt(SOUND, SOUND_DEFAULT);
		int prefDebugLog = pref.getInt(DEBUGLOG, DEBUGLOG_DEFAULT);
		int prefLogfile = pref.getInt(LOGFILE, LOGFILE_DEFAULT);

		activate = ON_OFF.values()[prefActivate];
		button_activate.setText(activateText[prefActivate]);

		toggleActivateButtonBackground();

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

		logfile = ON_OFF.values()[prefLogfile];
		button_logfile.setText(logfileText[prefLogfile]);

		showToast = true;

		startSound();
	}

	@Override
	protected void onPause() {
		super.onPause();

		showToast = false;

		pref.edit().putInt(ACTIVATE, activate.ordinal()).apply();
		pref.edit().putInt(CORRECTGLYPHS, correctGlyphs.ordinal()).apply();
		pref.edit().putInt(GLYPHKEY, glyphKey.ordinal()).apply();
		pref.edit().putInt(GLYPHSPEED, glyphSpeed.ordinal()).apply();
		pref.edit().putInt(NORMALHACKKEY, normalHack.ordinal()).apply();
		pref.edit().putInt(SOUND, sound.ordinal()).apply();
		pref.edit().putInt(DEBUGLOG, debugLog.ordinal()).apply();
		pref.edit().putInt(LOGFILE, logfile.ordinal()).apply();

		exitSound();

		showToast = true;
	}

	private void setListeners() {
		button_activate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				activate = ON_OFF.values()[(activate.ordinal() + 1) % ON_OFF.values().length];
				button_activate.setText(activateText[activate.ordinal()]);

				toggleActivateButtonBackground();

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

		button_logfile.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				logfile = ON_OFF.values()[(logfile.ordinal() + 1) % ON_OFF.values().length];
				button_logfile.setText(logfileText[logfile.ordinal()]);

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

	// FIXME: experimental code, deprecated API, don't use this at the moment
	private void toggleActivateButtonBackground() {
		/*if (activate == ON_OFF.OFF) {
			button_activate.setBackgroundDrawable(getResources().getDrawable(R.drawable.rectangle_red));
		} else {
			button_activate.setBackgroundDrawable(getResources().getDrawable(R.drawable.rectangle_cyan));
		}*/
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

		description.requestWindowFeature(Window.FEATURE_NO_TITLE);
		description.getWindow().setBackgroundDrawableResource(R.color.transparent);
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

	private PackageInfo findPackage(String packageName) throws AppNotInstalledException {
		List<PackageInfo> installedPackages = getPackageManager().getInstalledPackages(0);

		for (PackageInfo installedPackage : installedPackages) {
			if (installedPackage.packageName.equals(packageName)) {
				return installedPackage;
			}
		}

		throw new AppNotInstalledException();
	}

	private boolean isXposedAvailable() {
		if (System.getenv("CLASSPATH") != null) {
			if (System.getenv("CLASSPATH").contains("Xposed")) {
				return true;
			}
		}

		return false;
	}

	private boolean isXposedEnabled() {
		// This method will be hooked to return true
		return false;
	}

	private void descriptionHint() {
		Button button = new Button(this);
		button.setBackgroundColor(0); // transparent

		new ShowcaseView.Builder(this)
				.withNewStyleShowcase()
				.setStyle(R.style.ShowcaseViewStyle)
				.setTarget(new ViewTarget(R.id.description_glyphGame, this))
				.setContentText(R.string.description_showView)
				.replaceEndButton(button)
				.hideOnTouchOutside()
				.singleShot(4242) // don't care about an ID, because this is the only ShowcaseView
				.build();
	}
}
