package me.velc.mwcm;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static me.velc.mwcm.Constants.TARGET_NAME;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class HookLogo implements IXposedHookLoadPackage {

	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) {
		if (!TARGET_NAME.equals(lpparam.packageName)) return;
		findAndHookMethod(
				TARGET_NAME + ".syssetting.ui.viewholder.LogoViewHolder",
				lpparam.classLoader,
				"setupFromData",
				TARGET_NAME + ".ssetting.model.SSettingItem",
				new XC_MethodHook() {
					@Override
					protected void afterHookedMethod(MethodHookParam param) {
						hookLogo(param.thisObject);
					}
				}
		);
	}

	@SuppressLint("DiscouragedApi, SetTextI18n")
	static void hookLogo(Object viewHolder) {
		var itemView = (View) getObjectField(viewHolder, "itemView");

		int logoId = itemView.getResources().getIdentifier(
				"img_icon", "id", TARGET_NAME);
		itemView.findViewById(logoId).setOnClickListener(v -> {
			var loader = viewHolder.getClass().getClassLoader();
			var mainActivity = findClass(TARGET_NAME + ".app.main.AppMainActivity", loader);
			v.getContext().startActivity(new Intent(v.getContext(), mainActivity));
		});

		var titleId = itemView.getResources().getIdentifier(
				"tv_title", "id", TARGET_NAME);
		int appNameId = itemView.getResources().getIdentifier(
				"app_name", "string", TARGET_NAME);
		var title = itemView.<TextView>findViewById(titleId);
		title.setText("\uD83E\uDE9D " + itemView.getContext().getString(appNameId) + " \uD83E\uDE9D");
	}
}