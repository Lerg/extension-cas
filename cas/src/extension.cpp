#include "extension.h"

// This is the entry point of the extension. It defines Lua API of the extension.

static const luaL_reg lua_functions[] = {
	{"init", EXTENSION_INIT},
	{"validate_integration", EXTENSION_VALIDATE_INTEGRATION},
	{"load", EXTENSION_LOAD},
	{"is_loaded", EXTENSION_IS_LOADED},
	{"show", EXTENSION_SHOW},
	{"hide_banner", EXTENSION_HIDE_BANNER},
	{"set", EXTENSION_SET},
	{0, 0}
};

enum LUA_CONSTS {
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

dmExtension::Result APP_INITIALIZE(dmExtension::AppParams *params) {
	return dmExtension::RESULT_OK;
}

dmExtension::Result APP_FINALIZE(dmExtension::AppParams *params) {
	return dmExtension::RESULT_OK;
}

dmExtension::Result INITIALIZE(dmExtension::Params *params) {
	luaL_register(params->m_L, EXTENSION_NAME_STRING, lua_functions);

	#define SET_FIELD(name) lua_pushnumber(params->m_L, name); lua_setfield(params->m_L, -2, #name);

	SET_FIELD(BANNER)
	SET_FIELD(INTERSTITIAL)
	SET_FIELD(REWARDED)

	SET_FIELD(INIT)
	SET_FIELD(LOADED)
	SET_FIELD(FAILED_TO_LOAD)
	SET_FIELD(SHOWN)
	SET_FIELD(FAILED)
	SET_FIELD(CLICKED)
	SET_FIELD(COMPLETE)
	SET_FIELD(CLOSED)

	SET_FIELD(TAGGED_AUDIENCE)
	SET_FIELD(AUDIENCE_CHILDREN)
	SET_FIELD(AUDIENCE_NOT_CHILDREN)
	SET_FIELD(AUDIENCE_UNDEFINED)

	SET_FIELD(USER_CONSENT)
	SET_FIELD(CONSENT_ACCEPTED)
	SET_FIELD(CONSENT_DENIED)

	SET_FIELD(CCPA)
	SET_FIELD(CCPA_OPT_IN_SALE)
	SET_FIELD(CCPA_OPT_OUT_SALE)

	SET_FIELD(MUTED_AD_SOUNDS)

	SET_FIELD(TARGETING_AGE)
	SET_FIELD(TARGETING_GENDER)
	SET_FIELD(TARGETING_KEYWORDS)

	SET_FIELD(GENDER_UNKNOWN)
	SET_FIELD(GENDER_MALE)
	SET_FIELD(GENDER_FEMALE)

	#undef SET_FIELD

	lua_pop(params->m_L, 1);
	EXTENSION_INITIALIZE(params->m_L);
	return dmExtension::RESULT_OK;
}

dmExtension::Result UPDATE(dmExtension::Params *params) {
	EXTENSION_UPDATE(params->m_L);
	return dmExtension::RESULT_OK;
}

void EXTENSION_ON_EVENT(dmExtension::Params *params, const dmExtension::Event *event) {
	switch (event->m_Event) {
		case dmExtension::EVENT_ID_ACTIVATEAPP:
			EXTENSION_APP_ACTIVATE(params->m_L);
			break;
		case dmExtension::EVENT_ID_DEACTIVATEAPP:
			EXTENSION_APP_DEACTIVATE(params->m_L);
			break;
	}
}

dmExtension::Result FINALIZE(dmExtension::Params *params) {
	EXTENSION_FINALIZE(params->m_L);
	return dmExtension::RESULT_OK;
}

DM_DECLARE_EXTENSION(EXTENSION_NAME, EXTENSION_NAME_STRING, APP_INITIALIZE, APP_FINALIZE, INITIALIZE, UPDATE, EXTENSION_ON_EVENT, FINALIZE)
