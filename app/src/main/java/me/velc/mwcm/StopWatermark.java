package me.velc.mwcm;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.setBooleanField;
import static me.velc.mwcm.Constants.TARGET_NAME;

import android.content.Context;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class StopWatermark implements IXposedHookLoadPackage {

	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) {
		if (!TARGET_NAME.equals(lpparam.packageName)) return;
		findAndHookMethod(
				TARGET_NAME + ".wallpaper.ApplyDesktopWallpaper",
				lpparam.classLoader,
				"setWp",
				Context.class, TARGET_NAME + ".preview.model.WallpaperInfo",
				new XC_MethodHook() {
					@Override
					protected void beforeHookedMethod(MethodHookParam param) {
						setBooleanField(param.args[1], "supportLike", false);
					}
				}
		);
	}
}