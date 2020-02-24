package io.flutter.plugins.urllauncher;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Browser;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import java.io.File;
import java.util.List;

import static android.util.Log.println;
import static androidx.core.content.ContextCompat.startActivity;

/** Launches components for URLs. */
class UrlLauncher {
  private final Context applicationContext;
  @Nullable private Activity activity;

  /**
   * Uses the given {@code applicationContext} for launching intents.
   *
   * <p>It may be null initially, but should be set before calling {@link #launch}.
   */
  UrlLauncher(Context applicationContext, @Nullable Activity activity) {
    this.applicationContext = applicationContext;
    this.activity = activity;
  }

  void setActivity(@Nullable Activity activity) {
    this.activity = activity;
  }

  /** Returns whether the given {@code url} resolves into an existing component. */
  boolean canLaunch(String url) {
    Intent launchIntent = new Intent(Intent.ACTION_VIEW);
    launchIntent.setData(Uri.parse(url));
    ComponentName componentName =
        launchIntent.resolveActivity(applicationContext.getPackageManager());

    return componentName != null
        && !"{com.android.fallback/com.android.fallback.Fallback}"
            .equals(componentName.toShortString());
  }


  boolean launchApp(String packageName){
    Intent resolveIntent  = new Intent(Intent.ACTION_MAIN);
    resolveIntent .addCategory(Intent.CATEGORY_LAUNCHER);
    resolveIntent .setPackage(packageName);
    List<ResolveInfo> resolveinfoList = applicationContext.getPackageManager()
            .queryIntentActivities(resolveIntent, 0);
    if(resolveinfoList.size()==0){
      Log.i("launchApp","false");
      return false;
    }
    ResolveInfo resolveinfo = resolveinfoList.iterator().next();
    for(int i=0;i<resolveinfoList.size();i++){
      Log.i("packageName",resolveinfoList.get(i).activityInfo.name);
    }
    if (resolveinfo != null) {
      // packagename = 参数packname
      // 这个就是我们要找的该APP的LAUNCHER的Activity[组织形式：packagename.mainActivityname]
      String className = resolveinfo.activityInfo.name;
      // LAUNCHER Intent
      Intent intent = new Intent(Intent.ACTION_MAIN);
      intent.addCategory(Intent.CATEGORY_LAUNCHER);
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      // 设置ComponentName参数1:packagename参数2:MainActivity路径
      ComponentName cn = new ComponentName(packageName, className);

      intent.setComponent(cn);
      applicationContext.startActivity(intent);
      Log.i("launchApp","true");
      return true;
    }else{
      return false;
    }

  }
  /**
   * Attempts to launch the given {@code url}.
   *
   * @param headersBundle forwarded to the intent as {@code Browser.EXTRA_HEADERS}.
   * @param useWebView when true, the URL is launched inside of {@link WebViewActivity}.
   * @param enableJavaScript Only used if {@param useWebView} is true. Enables JS in the WebView.
   * @param enableDomStorage Only used if {@param useWebView} is true. Enables DOM storage in the
   * @return {@link LaunchStatus#NO_ACTIVITY} if there's no available {@code applicationContext}.
   *     {@link LaunchStatus#OK} otherwise.
   */
  LaunchStatus launch(
      String url,
      Bundle headersBundle,
      boolean useWebView,
      boolean enableJavaScript,
      boolean enableDomStorage) {
    if (activity == null) {
      return LaunchStatus.NO_ACTIVITY;
    }

    Intent launchIntent;
    if (useWebView) {
      launchIntent =
          WebViewActivity.createIntent(
              activity, url, enableJavaScript, enableDomStorage, headersBundle);
    } else {
      launchIntent =
          new Intent(Intent.ACTION_VIEW)
              .setData(Uri.parse(url))
              .putExtra(Browser.EXTRA_HEADERS, headersBundle);
    }

    activity.startActivity(launchIntent);
    return LaunchStatus.OK;
  }

  /** Closes any activities started with {@link #launch} {@code useWebView=true}. */
  void closeWebView() {
    applicationContext.sendBroadcast(new Intent(WebViewActivity.ACTION_CLOSE));
  }

  /** Result of a {@link #launch} call. */
  enum LaunchStatus {
    /** The intent was well formed. */
    OK,
    /** No activity was found to launch. */
    NO_ACTIVITY,
  }
}
