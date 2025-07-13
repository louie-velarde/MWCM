package me.velc.mwcar;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.getStaticIntField;
import static de.robv.android.xposed.XposedHelpers.getStaticObjectField;
import static de.robv.android.xposed.XposedHelpers.newInstance;
import static me.velc.mwcar.Constants.TARGET_NAME;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class ChangeWallpaper implements IXposedHookLoadPackage {

	@Override
	public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
		if (!TARGET_NAME.equals(lpparam.packageName)) return;
		interceptUncheck("ProviderEnableSwitcherClickAction",
		                 lpparam.classLoader,
		                 "WALLPAPER_SOURCE_TYPE_LOCKSCREEN");
		interceptUncheck("DesktopProviderEnableSwitcherClickAction",
		                 lpparam.classLoader,
		                 "WALLPAPER_SOURCE_TYPE_DESKTOP");
	}

	static void interceptUncheck(String cName, ClassLoader cLoader, String sourceFieldName) {
		findAndHookMethod(
				TARGET_NAME + ".ssetting.clickaction." + cName,
				cLoader,
				"onClick",
				View.class,
				new OnClickHook(cLoader, sourceFieldName)
		);
	}

	private static class OnClickHook extends XC_MethodHook {

		private final int ACTION_BOTH;
		private final int ACTION_HOME;
		private final int ACTION_LOCK;

		private final String SOURCE;

		private final Class<?> controllerClass;
		private final Class<?> managerClass;
		private final Class<?> status;

		private final Class<?> taskBuilderClass;
		private final Class<?> taskCallbackClass;

		OnClickHook(ClassLoader loader, String sourceFieldName) {
			var const1 = findClass(TARGET_NAME + ".wallpaper.ApplyWallpaper", loader);
			ACTION_BOTH = getStaticIntField(const1, "ACTION_TYPE_BOTH");
			ACTION_HOME = getStaticIntField(const1, "ACTION_TYPE_DESKTOP");
			ACTION_LOCK = getStaticIntField(const1, "ACTION_TYPE_LOCKSCREEN");

			var const2 = findClass(TARGET_NAME + ".wallpaper.IWallpaperConstants", loader);
			SOURCE = (String) getStaticObjectField(const2, sourceFieldName);

			controllerClass = findClass(TARGET_NAME + ".wallpaper.WallpaperController", loader);
			managerClass = findClass(TARGET_NAME + ".lks.WallpaperManager", loader);
			status = findClass(TARGET_NAME + ".lks.ProviderStatus", loader);

			taskBuilderClass = findClass(const1.getName() + "$ApplyWallpaperParmsBuilder", loader);
			taskCallbackClass = findClass(const1.getName() + "$IResult", loader);
		}

		@Override
		protected void beforeHookedMethod(MethodHookParam param) {
			var checkBox = getObjectField(param.thisObject, "mSideBtn");
			var isChecked = (Boolean) callMethod(checkBox, "isChecked");

			var controller = callStaticMethod(controllerClass, "getInstance");
			var isEnabled = (Boolean) callMethod(controller, "enable");
			if (!isChecked || !isEnabled) return;

			var ctx = ((View) param.args[0]).getContext();
			var task = buildTask(getAction(ctx), getWallpaper());
			callMethod(task, "applyWallpaper", makeCallback(new CallbackHandler() {
				@Override
				protected Object handleCallback(Object proxy, Method method, Object[] args) {
					if ((Boolean) args[0]) {
						int resId = ctx.getResources().getIdentifier(
								"lks_subscription_toast", "string", TARGET_NAME);
						Toast.makeText(ctx, resId, Toast.LENGTH_SHORT).show();
					}
					return null;
				}
			}));
			param.setResult(null);
		}

		private int getAction(Context ctx) {
			var home = (Boolean) callStaticMethod(status, "isDesktopProviderWorking");
			var lock = (Boolean) callStaticMethod(status, "isLockscreenMagazineWorking", ctx);
			return home && lock ? ACTION_BOTH : home ? ACTION_HOME : ACTION_LOCK;
		}

		private Object getWallpaper() {
			var manager = callStaticMethod(managerClass, "getInstance");
			return callMethod(manager, "getNextWallpaper", SOURCE, "");
		}

		private Object buildTask(int action, Object wallpaper) {
			var taskBuilder = newInstance(taskBuilderClass);
			callMethod(taskBuilder, "applyAction", action);
			callMethod(taskBuilder, "setParms", wallpaper);
			return callMethod(taskBuilder, "build");
		}

		private Object makeCallback(CallbackHandler handler) {
			var loader = taskCallbackClass.getClassLoader();
			var interfaces = new Class[]{taskCallbackClass};
			return Proxy.newProxyInstance(loader, interfaces, handler);
		}
	}
}