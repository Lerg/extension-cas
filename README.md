# CAS Extension for Defold

The CAS.AI (CleverAdsSolutions) extension lets you display banner, interstitial and rewarded ads.

Official website https://cas.ai
Official andrdoid documentation https://github.com/cleveradssolutions/CAS-Android/wiki

NOTE: Banners are not implemented. Extension supports Android only for now.

# Setup

Open `game.project` and add a new entry to the `dependencies` property:

```
https://github.com/Lerg/extension-cas/archive/master.zip
```

Then select `Project -> Fetch Libraries` to download the extension in your project.

If you are using Admob you need to set the AdMob App Id for your iOS and/or Android app in `game.project`. To do so open your `game.project` and add these lines:
```
[cas]
admob_android_app_id = ca-app-pub-***~***
```

To control which ad providers are integrated add these lines to the `[cas]` section in `game.project`:
```
adcolony_android = 1
admob_android = 1
applovin_android = 1
bigo_android = 1
charboost_android = 1
facebook_android = 1
fyber_android = 1
hyprmx_android = 1
inmobi_android = 0
ironsource_android = 1
kidoz_android = 0
liftoff_android = 1
mintegral_android = 1
mytarget_android = 0
pangle_android = 1
superawesome_android = 0
unityads_android = 1
yandex_android = 1
```

Replace `ca-app-pub-***~***` with your app ids. It can now be viewed or changed in the normal view of the `game.project` file.

# API reference

### cas.init(`params`: _table_)

Initializes the extension. This function has to be called first before trying to show any ads.

- `params`: _table_ Contains parameters for the call &mdash; see the next section for details.
	- `id`: _string_ CAS application ID.
	- `disable_banner`: _boolean_ If `true` no banners loading.
	- `disable_interstitial`: _boolean_ If `true` no interstitial loading.
	- `disable_rewarded`: _boolean_ If `true` no rewarded loading.
	- `test_device`: _string_ Test device ID to enable testing on this device.
	- `test`: _boolean_ If `true` the test ads will be served. ALWAYS use the test ads during development.
	- `debug`: _boolean_ If `true` additional debug messages.
	- `listener`: _function_ The callback function which receives all CAS events.

---
### cas.validate_integration()
Prints to console SDK integration information.

---
### cas.load(`ad_type`: _number_)

Loads a specified ad type. No need to call this function, the ads is loaded automatically.

- `ad_type`: _number_ `cas.BANNER`, `cas.INTERSTITIAL` or `cas.REWARDED`.

---
### cas.is_loaded(`ad_type`: _number_) -> `is_loaded`: _boolean_

Returns `true` if the specified ad type has been loaded.

- `ad_type`: _number_ `cas.BANNER`, `cas.INTERSTITIAL` or `cas.REWARDED`.
- return: `is_loaded`: _boolean_.

---
### cas.show(`ad_type`: _number_)

Displays a loaded ad.

- `ad_type`: _number_ `cas.BANNER`, `cas.INTERSTITIAL` or `cas.REWARDED`.

---

### cas.hide_banner()

Removes a loaded banner from the screen.

### cas.set(`param`: _number_, `value`: _number_|_boolean_|_table_)

Changes settings.

- `param`: _number_ Parameter id.
- `value`: _number_|_boolean_|_table_ Value id or value itself.

- `cas.TAGGED_AUDIENCE`:
	- `cas.AUDIENCE_CHILDREN`
	- `cas.AUDIENCE_NOT_CHILDREN`
	- `cas.AUDIENCE_UNDEFINED`
- `cas.USER_CONSENT`:
	- `cas.CONSENT_ACCEPTED`
	- `cas.CONSENT_DENIED`
- `cas.CCPA`:
	- `cas.CCPA_OPT_IN_SALE`
	- `cas.CCPA_OPT_OUT_SALE`
- `cas.TARGETING_GENDER`:
	- `cas.GENDER_UNKNOWN`
	- `cas.GENDER_MALE`
	- `cas.GENDER_FEMALE`
- `cas.TARGETING_AGE`:
	- `value`: _number_ Age as a number.
- `cas.TARGETING_KEYWORDS`:
	- `value`: _table_ Array of string keywords.
- `cas.MUTED_AD_SOUNDS`:
	- `value`: _boolean_ Muted if `true`.

---

## CAS Events

### `event`: _table_

- `phase`: _number_ Event phase.
- `type`: _number_ Event type.

#### Initialization
- `phase`: `cas.INIT`
- `type`: `cas.INIT`
- `protection_applied`: _boolean_.
- `user_country_iso2O`: _string_ Nullable.
- `error`: _string_ Nullable.

#### Ad load
- `phase`:
	- `cas.LOADED`
	- `cas.FAILED_TO_LOAD`
- `type`:
	- `cas.BANNER`
	- `cas.INTERSTITIAL`
	- `cas.REWARDED`.

#### Ad show
- `phase`:
	- `cas.SHOWN`
	- `cas.FAILED`
	- `cas.CLICKED`
	- `cas.COMPLETE`
	- `cas.CLOSED`
- `type`:
	- `cas.INTERSTITIAL`
	- `cas.REWARDED`

# Patreon

If you like this extension please consider supporting me on Patreon https://patreon.com/Lerg
