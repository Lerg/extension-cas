package extension.cas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.HashSet;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cleversolutions.ads.*;
import com.cleversolutions.ads.android.CAS;

import extension.cas.Utils.Scheme;
import extension.cas.Utils.Table;

@SuppressWarnings("unused")
public class Extension {
	private Activity activity;
	private LinearLayout main_layout;
	private String banner_position;
	private PopupWindow popup;
	private boolean is_initialized = false;
	private LuaScriptListener script_listener = new LuaScriptListener();

	private MediationManager manager;

	private enum LuaConsts {
		BANNER,
		INTERSTITIAL,
		REWARDED,

		INIT,
		LOADED,
		FAILED_TO_LOAD,
		SHOWN,
		FAILED,
		CLICKED,
		COMPLETE,
		CLOSED,

		TAGGED_AUDIENCE,
		AUDIENCE_CHILDREN,
		AUDIENCE_NOT_CHILDREN,
		AUDIENCE_UNDEFINED,

		USER_CONSENT,
		CONSENT_ACCEPTED,
		CONSENT_DENIED,

		CCPA,
		CCPA_OPT_IN_SALE,
		CCPA_OPT_OUT_SALE,

		MUTED_AD_SOUNDS,

		TARGETING_AGE,
		TARGETING_GENDER,
		TARGETING_KEYWORDS,

		GENDER_UNKNOWN,
		GENDER_MALE,
		GENDER_FEMALE
	};

	@SuppressWarnings("unused")
	public Extension(android.app.Activity main_activity) {
		activity = main_activity;
		Utils.set_tag("cas");
	}

	// Called from extension_android.cpp each frame.
	@SuppressWarnings("unused")
	public void update(long L) {
		Utils.execute_tasks(L);
	}

	@SuppressWarnings("unused")
	public void app_activate(long L) {
	}

	@SuppressWarnings("unused")
	public void app_deactivate(long L) {
	}

	@SuppressWarnings("unused")
	public void extension_finalize(long L) {
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	private boolean check_is_initialized() {
		if (is_initialized) {
			return true;
		} else {
			Utils.log("The extension is not initialized.");
			return false;
		}
	}

	//region Lua functions

	// cas.init(params)
	private int init(long L) {
		Utils.debug_log("init()");
		Utils.check_arg_count(L, 1);
		Scheme scheme = new Scheme()
			.string("id")
			.bool("disable_banner")
			.bool("disable_interstitial")
			.bool("disable_rewarded")
			//.bool("manual_loading")
			.string("test_device")
			.bool("test")
			.bool("debug")
			.function("listener");

		Table params = new Table(L, 1).parse(scheme);
		String id = params.get_string_not_null("id");
		boolean disable_banner = params.get_boolean("disable_banner", false);
		boolean disable_interstitial = params.get_boolean("disable_interstitial", false);
		boolean disable_rewarded = params.get_boolean("disable_rewarded", false);
		//boolean is_manual_loading = params.get_boolean("manual_loading", false);
		String test_device = params.get_string("test_device");
		boolean is_test = params.get_boolean("test", false);
		boolean is_debug = params.get_boolean("debug", false);

		Utils.delete_ref_if_not_nil(L, script_listener.listener);
		Utils.delete_ref_if_not_nil(L, script_listener.script_instance);
		script_listener.listener = params.get_function("listener", Lua.REFNIL);
		Lua.dmscript_getinstance(L);
		script_listener.script_instance = Utils.new_ref(L);

		final Extension _this = this;
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (test_device != null) {
					CAS.getSettings().setTestDeviceIDs(new HashSet<>(Arrays.asList(test_device)));
				}
				if (is_debug) {
					CAS.getSettings().setDebugMode(true);
				}
				ArrayList<AdType> ad_types = new ArrayList<AdType>();
				if (!disable_banner) {
					ad_types.add(AdType.Banner);
				}
				if (!disable_interstitial) {
					ad_types.add(AdType.Interstitial);
				}
				if (!disable_rewarded) {
					ad_types.add(AdType.Rewarded);
				}
				manager = CAS.buildManager()
					// Set your CAS ID
					.withCasId(id)
					.withCompletionListener(config -> {
						String initErrorOrNull = config.getError();
						String userCountryISO2OrNull = config.getCountryCode();
						boolean protectionApplied = config.isConsentRequired();
						MediationManager manager = config.getManager();

						Hashtable<Object, Object> event = Utils.new_event();
						event.put("phase", LuaConsts.INIT.ordinal());
						event.put("type", LuaConsts.INIT.ordinal());
						event.put("protection_applied", protectionApplied);
						if (userCountryISO2OrNull != null) {
							event.put("user_country_iso2O", userCountryISO2OrNull);
						}
						if (initErrorOrNull != null) {
							event.put("error", initErrorOrNull);
						} else {
							is_initialized = true;
						}
						Utils.dispatch_event(script_listener, event);
					})
					// List Ad formats used in app
					.withAdTypes(ad_types.toArray(new AdType[0]))
					// Use Test ads or live ads
					.withTestAdMode(is_test)
					.initialize(activity);
				manager.getOnAdLoadEvent().add(adLoadCallback);
			}
		});

		return 0;
	}

	// cas.validate_integration()
	private int validate_integration(long L) {
		Utils.check_arg_count(L, 0);
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				CAS.validateIntegration(activity);
			}
		});
		return 0;
	}

	// cas.load(type)
	private int load(long L) {
		Utils.debug_log("is_loaded()");
		Utils.check_arg_count(L, 1);

		if (!check_is_initialized()) return 0;
		if (Lua.type(L, 1) != Lua.Type.NUMBER) return 0;

		final int type = (int)Lua.tonumber(L, 1);

		if (type == LuaConsts.INTERSTITIAL.ordinal()) {
			manager.loadInterstitial();
		} else if (type == LuaConsts.REWARDED.ordinal()) {
			manager.loadRewardedAd();
		} else if (type == LuaConsts.BANNER.ordinal()) {
		}

		return 0;
	}

	// cas.is_loaded(type)
	private int is_loaded(long L) {
		Utils.debug_log("is_loaded()");
		Utils.check_arg_count(L, 1);

		if (!check_is_initialized()) return 0;
		if (Lua.type(L, 1) != Lua.Type.NUMBER) return 0;

		final int type = (int)Lua.tonumber(L, 1);

		if (type == LuaConsts.INTERSTITIAL.ordinal()) {
			Lua.pushboolean(L, manager.isInterstitialReady());
			return 1;
		} else if (type == LuaConsts.REWARDED.ordinal()) {
			Lua.pushboolean(L, manager.isRewardedAdReady());
			return 1;
		} else if (type == LuaConsts.BANNER.ordinal()) {
			Lua.pushboolean(L, false);
			return 1;
		}

		return 0;
	}

	// cas.show(type)
	private int show(long L) {
		Utils.debug_log("show()");
		Utils.check_arg_count(L, 1);

		if (!check_is_initialized()) return 0;
		if (Lua.type(L, 1) != Lua.Type.NUMBER) return 0;

		final int type = (int)Lua.tonumber(L, 1);

		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (type == LuaConsts.INTERSTITIAL.ordinal()) {
					Utils.debug_log("show interstitial");
					manager.showInterstitial(activity, interstitialCallback);
				} else if (type == LuaConsts.REWARDED.ordinal()) {
					Utils.debug_log("show rewarded");
					manager.showRewardedAd(activity, rewardedCallback);
				}
			}
		});

		return 0;
	}

	// cas.hide_banner()
	private int hide_banner(long L) {
		Utils.debug_log("hide_banner()");
		Utils.check_arg_count(L, 0);
		if (!check_is_initialized()) return 0;
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				/*if (banner != null) {
					banner.destroy();
					banner = null;
				}
				if (popup != null) {
					popup.dismiss();
					popup = null;
				}*/
			}
		});
		return 0;
	}

	// cas.set(param, value)
	private int set(long L) {
		Utils.check_arg_count(L, 2);
		final int param_index = 1;
		final int value_index = 2;
		if (Lua.type(L, param_index) != Lua.Type.NUMBER) return 0;
		final int param = (int)Lua.tonumber(L, param_index);

		if (param == LuaConsts.TAGGED_AUDIENCE.ordinal()
		|| param == LuaConsts.USER_CONSENT.ordinal()
		|| param == LuaConsts.CCPA.ordinal()
		|| param == LuaConsts.TARGETING_AGE.ordinal()
		|| param == LuaConsts.TARGETING_GENDER.ordinal()) {
			if (Lua.type(L, value_index) != Lua.Type.NUMBER) return 0;
			final int value = (int)Lua.tonumber(L, value_index);

			if (param == LuaConsts.TAGGED_AUDIENCE.ordinal()) {
				if (value == LuaConsts.AUDIENCE_CHILDREN.ordinal()) {
					CAS.getSettings().setTaggedAudience(Audience.CHILDREN);
				} else if (value == LuaConsts.AUDIENCE_NOT_CHILDREN.ordinal()) {
					CAS.getSettings().setTaggedAudience(Audience.NOT_CHILDREN);
				} else if (value == LuaConsts.AUDIENCE_UNDEFINED.ordinal()) {
					CAS.getSettings().setTaggedAudience(Audience.UNDEFINED);
				}
			} else if (param == LuaConsts.USER_CONSENT.ordinal()) {
				if (value == LuaConsts.CONSENT_ACCEPTED.ordinal()) {
					CAS.getSettings().setUserConsent(ConsentStatus.ACCEPTED);
				} else if (value == LuaConsts.CONSENT_DENIED.ordinal()) {
					CAS.getSettings().setUserConsent(ConsentStatus.DENIED);
				}
			} else if (param == LuaConsts.CCPA.ordinal()) {
				if (value == LuaConsts.CCPA_OPT_IN_SALE.ordinal()) {
					CAS.getSettings().setCcpaStatus(CCPAStatus.OPT_IN_SALE);
				} else if (value == LuaConsts.CCPA_OPT_OUT_SALE.ordinal()) {
					CAS.getSettings().setCcpaStatus(CCPAStatus.OPT_OUT_SALE);
				}
			} else if (param == LuaConsts.TARGETING_GENDER.ordinal()) {
				if (value == LuaConsts.GENDER_MALE.ordinal()) {
					CAS.getTargetingOptions().setGender(TargetingOptions.GENDER_MALE);
				} else if (value == LuaConsts.GENDER_FEMALE.ordinal()) {
					CAS.getTargetingOptions().setGender(TargetingOptions.GENDER_FEMALE);
				}
			} else if (param == LuaConsts.TARGETING_AGE.ordinal()) {
				CAS.getTargetingOptions().setAge(value);
			}
		} else if (param == LuaConsts.MUTED_AD_SOUNDS.ordinal()) {
			if (Lua.type(L, value_index) != Lua.Type.BOOLEAN) return 0;
			final boolean value = Lua.toboolean(L, value_index);
			CAS.getSettings().setMutedAdSounds(value);
		} else if (param == LuaConsts.TARGETING_KEYWORDS.ordinal()) {
			if (Lua.type(L, value_index) != Lua.Type.TABLE) return 0;
			HashSet<String> keywords = new HashSet<String>();
			final int array_length = (int)Lua.objlen(L, value_index);
			for (int i = 1; i <= array_length; ++i) {
				Lua.rawget(L, value_index, i);
				if (Lua.type(L, -1) == Lua.Type.STRING) {
					keywords.add(Lua.tostring(L, -1));
				}
				Lua.pop(L, 1);
			}
			CAS.getTargetingOptions().setKeywords(keywords);
		}
		return 0;
	}
	//endregion

	//region Callbacks
	private void dispatch_event(LuaConsts phase, LuaConsts event_type) {
		Hashtable<Object, Object> event = Utils.new_event();
		event.put("phase", phase.ordinal());
		event.put("type", event_type.ordinal());
		Utils.dispatch_event(script_listener, event);
	}

	private AdLoadCallback adLoadCallback = new AdLoadCallback() {
		@Override
		public void onAdLoaded(@NonNull AdType type) {
			switch (type) {
				case Banner:
					dispatch_event(LuaConsts.LOADED, LuaConsts.BANNER);
					break;
				case Interstitial:
					dispatch_event(LuaConsts.LOADED, LuaConsts.INTERSTITIAL);
					break;
				case Rewarded:
					dispatch_event(LuaConsts.LOADED, LuaConsts.REWARDED);
					break;
			}
		}

		@Override
		public void onAdFailedToLoad(@NonNull AdType type, @Nullable String error) {
			switch (type) {
				case Banner:
					dispatch_event(LuaConsts.FAILED_TO_LOAD, LuaConsts.BANNER);
					break;
				case Interstitial:
					dispatch_event(LuaConsts.FAILED_TO_LOAD, LuaConsts.INTERSTITIAL);
					break;
				case Rewarded:
					dispatch_event(LuaConsts.FAILED_TO_LOAD, LuaConsts.REWARDED);
					break;
			}
		}
	};

	private AdCallback interstitialCallback = new AdCallback() {
		@Override
		public void onShown(@NonNull AdStatusHandler ad) {
			dispatch_event(LuaConsts.SHOWN, LuaConsts.INTERSTITIAL);
		}

		@Override
		public void onShowFailed(@NonNull String message) {
			dispatch_event(LuaConsts.FAILED, LuaConsts.INTERSTITIAL);
		}

		@Override
		public void onClicked() {
			dispatch_event(LuaConsts.CLICKED, LuaConsts.INTERSTITIAL);
		}

		@Override
		public void onComplete() {
			dispatch_event(LuaConsts.COMPLETE, LuaConsts.INTERSTITIAL);
		}

		@Override
		public void onClosed() {
			dispatch_event(LuaConsts.CLOSED, LuaConsts.INTERSTITIAL);
		}
	};

	private AdCallback rewardedCallback = new AdCallback() {
		@Override
		public void onShown(@NonNull AdStatusHandler ad) {
			dispatch_event(LuaConsts.SHOWN, LuaConsts.REWARDED);
		}

		@Override
		public void onShowFailed(@NonNull String message) {
			dispatch_event(LuaConsts.FAILED, LuaConsts.REWARDED);
		}

		@Override
		public void onClicked() {
			dispatch_event(LuaConsts.CLICKED, LuaConsts.REWARDED);
		}

		@Override
		public void onComplete() {
			dispatch_event(LuaConsts.COMPLETE, LuaConsts.REWARDED);
		}

		@Override
		public void onClosed() {
			dispatch_event(LuaConsts.CLOSED, LuaConsts.REWARDED);
		}
	};
	//endregion
}
