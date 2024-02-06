#if !defined(DM_PLATFORM_ANDROID)

#include <dmsdk/sdk.h>
#include "extension.h"

int EXTENSION_INIT(lua_State *L) {
	dmLogInfo("init");
	return 0;
}

int EXTENSION_VALIDATE_INTEGRATION(lua_State *L) {
	dmLogInfo("validate_integration");
	return 0;
}

int EXTENSION_LOAD(lua_State *L) {
	dmLogInfo("load");
	return 0;
}

int EXTENSION_IS_LOADED(lua_State *L) {
	dmLogInfo("is_loaded");
	return 0;
}

int EXTENSION_SHOW(lua_State *L) {
	dmLogInfo("show");
	return 0;
}

int EXTENSION_HIDE_BANNER(lua_State *L) {
	dmLogInfo("hide_banner");
	return 0;
}

int EXTENSION_SET(lua_State *L) {
	dmLogInfo("set");
	return 0;
}

void EXTENSION_INITIALIZE(lua_State *L) {
}

void EXTENSION_UPDATE(lua_State *L) {
}

void EXTENSION_APP_ACTIVATE(lua_State *L) {
}

void EXTENSION_APP_DEACTIVATE(lua_State *L) {
}

void EXTENSION_FINALIZE(lua_State *L) {
}

#endif
