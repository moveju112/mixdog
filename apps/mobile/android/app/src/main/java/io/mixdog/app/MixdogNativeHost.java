package io.mixdog.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.net.Uri;
import android.provider.Settings;
import android.webkit.WebView;
import androidx.webkit.JavaScriptReplyProxy;
import androidx.webkit.WebMessageCompat;
import androidx.webkit.WebViewCompat;
import androidx.webkit.WebViewFeature;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONTokener;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import java.lang.ref.WeakReference;
import org.json.JSONObject;

/**
 * `window.MixdogNativeHost` (shared/native-app.ts reads it): an AndroidX
 * WebMessageListener, so it exists only in pages whose origin matches the rules
 * (bundled pairing screen + saved relay origins) and every message is checked
 * again against that allow-list, main frame only. Anything else gets nothing.
 * Messages are JSON `{id, method, args}`; replies `{id, result}`. Slow work
 * (permission prompt, token fetch) finishes later and fires `mixdognativepush`.
 */
public final class MixdogNativeHost implements WebViewCompat.WebMessageListener {
  static final int PERMISSION_REQUEST = 4711;
  static final String NAME = "MixdogNativeHost";
  private static WeakReference<MixdogNativeHost> current = new WeakReference<>(null);

  private final MainActivity activity;
  private final MixdogAppUpdater updater;
  private final MixdogFileSave fileSave;
  // One token per loaded page: file-save handles belong to the page that began
  // them and die when the WebView navigates (touched on the UI thread only).
  private Object pageEpoch = new Object();

  MixdogNativeHost(MainActivity activity) {
    this.activity = activity;
    this.updater = new MixdogAppUpdater(activity, () -> dispatch("mixdognativeupdate"));
    this.fileSave = new MixdogFileSave(activity);
    current = new WeakReference<>(this);
  }

  /** The activity came back to the foreground (settings / installer closed). */
  void onResume() {
    updater.onResume();
    fileSave.onResume();
    // The notification permission may have changed in system settings.
    fireEvent();
  }

  /** This app's notification settings; the app's details page when the device has none. */
  private boolean openNotificationSettings() {
    Intent settings =
        new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, activity.getPackageName());
    Intent details =
        new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + activity.getPackageName()));
    for (Intent intent : new Intent[] {settings, details}) {
      try {
        activity.startActivity(intent);
        return true;
      } catch (Exception ignored) {
        // Not available here: try the next one.
      }
    }
    return false;
  }

  void onDestroy() {
    updater.close();
    fileSave.onDestroy();
  }

  /** The WebView started loading a page (UI thread): the previous page's save handles end. */
  static void pageStarted() {
    MixdogNativeHost host = current.get();
    if (host == null) return;
    Object previous = host.pageEpoch;
    host.pageEpoch = new Object();
    host.fileSave.release(previous);
  }

  /**
   * Register once, before the first page load. Removing and re-adding a
   * listener on a live page invalidates every reply proxy of that frame,
   * including Capacitor's bridge, and the next plugin reply crashes the
   * WebView natively. So the injection rule is every origin, and the
   * allow-list (bundled screen + saved relays, main frame) is enforced per
   * message in onPostMessage; navigation is limited by MixdogWebViewClient.
   */
  void register(WebView webView) {
    if (webView == null || !WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER)) return;
    WebViewCompat.addWebMessageListener(webView, NAME, Collections.singleton("*"), this);
    // [FORK:F003] 시작 — 폰 세션 서랍을 넓혀 오른쪽 빈 공간을 절반으로 줄임
    // 원본: relay 웹앱 CSS(apps/desktop/.../07-mobile-chrome.css)의 --mx-side-sheet-width = 화면의 78%
    // 포크: 세로 화면에서 --mx-side-sheet-width 를 89vw 로 덮어쓰는 스타일을 문서 시작 시 주입 (가로 화면은 원본 유지)
    // 이유: 서랍 오른쪽 빈 공간이 넓어 세션 제목 글자 공간이 좁음, PC 앱 포크 없이 폰 앱에서만 바꾸기 위함
    // 원본 동일 수정 시: 병합 — 원본이 서랍 폭을 넓히면 원본 채택, 변수명이 바뀌면 새 변수명으로 다시 적용
    if (WebViewFeature.isFeatureSupported(WebViewFeature.DOCUMENT_START_SCRIPT)) {
      WebViewCompat.addDocumentStartJavaScript(webView, DRAWER_WIDTH_SCRIPT, Collections.singleton("*"));
    }
    // [FORK:F003] 끝
    // [FORK:F006] 시작 — 폰에서 채팅 웹 링크를 Mixdog 브라우저 패널(Browser Use) 대신 시스템 브라우저로 열기
    // 원본: relay 웹앱의 linkOpenTarget 이 세션 브라우저 패널을 기본으로 선택
    // 포크: 링크 탭을 페이지보다 먼저(window capture) 가로채 다른 출처 http(s) 링크는 openExternalUrl 로 시스템 브라우저에 전달
    // 이유: 폰에서는 Browser Use 를 쓰지 않고 시스템 브라우저(Chrome 등)로 보기 위함
    // 원본 동일 수정 시: 병합 — 원본이 폰 기본값을 시스템 브라우저로 바꾸면 원본 채택, 링크 DOM 이 바뀌면 새 구조로 다시 적용
    if (WebViewFeature.isFeatureSupported(WebViewFeature.DOCUMENT_START_SCRIPT)) {
      WebViewCompat.addDocumentStartJavaScript(webView, SYSTEM_BROWSER_LINK_SCRIPT, Collections.singleton("*"));
    }
    // [FORK:F006] 끝
  }

  // [FORK:F006] 시작 — 링크 가로채기 스크립트 (relay 웹앱 폰 화면 html[data-mixdog-mobile-tabs] 에서만 동작)
  // 원본: 없음
  // 포크: 수정키 없는 왼쪽 탭 + 다른 출처 http(s) a[href] → 기본 동작·페이지 핸들러 차단 후 네이티브 openExternalUrl 호출 (id -1: 페이지의 응답 대기와 충돌 방지)
  // 이유: 위 register() 의 F006 블록과 동일
  // 원본 동일 수정 시: 병합 — 위 F006 블록과 함께 판단
  private static final String SYSTEM_BROWSER_LINK_SCRIPT =
      "(function(){window.addEventListener('click',function(e){"
          + "if(e.defaultPrevented||e.button!==0||e.ctrlKey||e.metaKey||e.shiftKey||e.altKey)return;"
          + "if(!document.documentElement.hasAttribute('data-mixdog-mobile-tabs'))return;"
          + "var host=window.MixdogNativeHost;if(!host||typeof host.postMessage!=='function')return;"
          + "var a=e.target&&e.target.closest?e.target.closest('a[href]'):null;if(!a)return;"
          + "var u;try{u=new URL(a.getAttribute('href'),location.href);}catch(_){return;}"
          + "if((u.protocol!=='http:'&&u.protocol!=='https:')||u.origin===location.origin)return;"
          + "e.preventDefault();e.stopImmediatePropagation();"
          + "host.postMessage(JSON.stringify({id:-1,method:'openExternalUrl',args:{url:u.href}}));"
          + "},true);})();";
  // [FORK:F006] 끝

  // [FORK:F003] 시작 — 서랍 폭 덮어쓰기 스타일 (html[data-mixdog-mobile-tabs] 는 relay 웹앱의 폰 화면에만 존재)
  // 원본: 없음
  // 포크: 세로 화면 서랍 폭 89vw
  // 이유: 위 register() 의 F003 블록과 동일
  // 원본 동일 수정 시: 병합 — 위 F003 블록과 함께 판단
  private static final String DRAWER_WIDTH_SCRIPT =
      "(function(){var s=document.createElement('style');"
          + "s.textContent='@media (orientation: portrait){html[data-mixdog-mobile-tabs]{--mx-side-sheet-width:89vw !important}}';"
          + "(document.head||document.documentElement).appendChild(s);})();";
  // [FORK:F003] 끝

  @Override
  public void onPostMessage(
      WebView view,
      WebMessageCompat message,
      Uri sourceOrigin,
      boolean isMainFrame,
      JavaScriptReplyProxy replyProxy) {
    String origin = OriginPolicy.originOf(sourceOrigin.toString());
    if (!isMainFrame || !OriginPolicy.isAllowed(origin, MixdogOrigins.saved(activity))) return;
    try {
      JSONObject request = new JSONObject(message.getData());
      JSONObject args = request.optJSONObject("args");
      int id = request.getInt("id");
      String method = request.getString("method");
      JSONObject safeArgs = args == null ? new JSONObject() : args;
      if (MixdogFileSave.handles(method)) {
        // Storage work runs off the UI thread; the reply arrives when it is done.
        fileSave.handle(method, safeArgs, pageEpoch, result -> activity.runOnUiThread(() -> reply(replyProxy, id, result)));
        return;
      }
      reply(replyProxy, id, handle(method, safeArgs, OriginPolicy.isBundled(origin)));
    } catch (Exception ignored) {
      // Malformed message: no answer.
    }
  }

  private static void reply(JavaScriptReplyProxy replyProxy, int id, String result) {
    try {
      replyProxy.postMessage(new JSONObject().put("id", id).put("result", new JSONTokener(result).nextValue()).toString());
    } catch (Exception ignored) {
      // The page is gone.
    }
  }

  /** Tell the web app the token / permission / parked actions changed. */
  static void notifyWeb() {
    MixdogNativeHost host = current.get();
    if (host != null) host.fireEvent();
  }

  private void fireEvent() {
    dispatch("mixdognativepush");
  }

  /** Fire a window event in the WebView (callable from any thread). */
  private void dispatch(String event) {
    WebView webView = activity.getBridge() == null ? null : activity.getBridge().getWebView();
    if (webView == null) return;
    webView.post(
        () ->
            webView.evaluateJavascript(
                "window.dispatchEvent(new Event('" + event + "'))", null));
  }

  private String permission() {
    boolean enabled = NotificationManagerCompat.from(activity).areNotificationsEnabled();
    boolean runtimeGranted =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
            || ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    if (enabled && runtimeGranted) return "granted";
    return MixdogPushStore.permissionAsked(activity) ? "denied" : "prompt";
  }

  private void fetchToken() {
    if (FirebaseApp.getApps(activity).isEmpty()) return; // no google-services.json in this build
    FirebaseMessaging.getInstance()
        .getToken()
        .addOnSuccessListener(
            token -> {
              if (token != null && !token.equals(MixdogPushStore.token(activity))) {
                MixdogPushStore.setToken(activity, token);
              }
              fireEvent();
            });
  }

  private JSONObject state() throws Exception {
    String permission = permission();
    if ("granted".equals(permission) && MixdogPushStore.token(activity).isEmpty()) fetchToken();
    return new JSONObject()
        .put("platform", "fcm")
        .put("token", MixdogPushStore.token(activity))
        .put("publicKey", MixdogKeys.publicKey(activity))
        .put("permission", permission);
  }

  void onPermissionResult() {
    fetchToken();
    fireEvent();
  }

  private String handle(String method, JSONObject args, boolean bundledPage) {
    try {
      switch (method) {
        case "setAllowedOrigins":
          // Only the bundled pairing screen may widen what can load.
          if (!bundledPage) return "false";
          JSONArray origins = args.optJSONArray("origins");
          List<String> list = new ArrayList<>();
          for (int i = 0; origins != null && i < origins.length(); i++) list.add(origins.optString(i));
          MixdogOrigins.save(activity, OriginPolicy.sanitize(list));
          return "true";
        case "getNativeCapabilities":
          return "{\"fileSave\":1}";
        case "getPushState":
          return state().toString();
        case "requestPush":
          if (!"granted".equals(permission())) {
            MixdogPushStore.setPermissionAsked(activity);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
              activity.runOnUiThread(
                  () ->
                      ActivityCompat.requestPermissions(
                          activity,
                          new String[] {Manifest.permission.POST_NOTIFICATIONS},
                          PERMISSION_REQUEST));
            }
          }
          return state().toString();
        case "openNotificationSettings":
          return openNotificationSettings() ? "true" : "false";
        case "peekPendingActions":
          return MixdogPushStore.peekActions(activity, args.optString("hostId", "")).toString();
        case "ackPendingAction":
          MixdogPushStore.ackAction(activity, args.optString("id", ""));
          return "true";
        case "takePendingAction":
          JSONObject action = MixdogPushStore.takeAction(activity);
          return action == null ? "null" : action.toString();
        case "getAppUpdate":
          return updater.get(args.optBoolean("check", false)).toString();
        case "startAppUpdate":
          return updater.start().toString();
        case "cancelAppUpdate":
          return updater.cancel().toString();
        case "installAppUpdate":
          return updater.install().toString();
        case "dismissAppUpdate":
          return updater.dismiss(args.optString("version", "")).toString();
        case "openAppUpdateInBrowser":
          return updater.openInBrowser() ? "true" : "false";
        // [FORK:F006] 시작 — 웹 링크를 안드로이드 시스템 브라우저로 열기
        // 원본: 없음
        // 포크: http/https 주소만 ACTION_VIEW 로 시스템 브라우저에 전달, 그 외 스킴은 거부
        // 이유: register() 의 F006 링크 가로채기 스크립트가 호출
        // 원본 동일 수정 시: 병합 — 원본에 같은 기능이 생기면 원본 메서드로 교체
        case "openExternalUrl":
          {
            Uri link = Uri.parse(args.optString("url", ""));
            String scheme = link.getScheme();
            if (!"https".equalsIgnoreCase(scheme) && !"http".equalsIgnoreCase(scheme)) return "false";
            activity.startActivity(new Intent(Intent.ACTION_VIEW, link).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            return "true";
          }
        // [FORK:F006] 끝
        case "openHostPicker":
          activity.runOnUiThread(
              () ->
                  activity
                      .getBridge()
                      .getWebView()
                      .loadUrl(activity.getBridge().getLocalUrl() + "/?manage=1"));
          return "true";
        default:
          return "null";
      }
    } catch (Exception e) {
      return "null";
    }
  }
}
