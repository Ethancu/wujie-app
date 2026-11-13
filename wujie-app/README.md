# 无界手机 App（WebView 壳）

这是一个**独立的 Android 工程**，不依赖 `rgfcc-clone` 目录，只通过网址加载你的站点。
之后改网页不用重新打包 App，只要网址不变。

## 两种“装到手机”的方式

### 方式一：PWA（零打包，推荐先用）

手机浏览器打开站点 → 菜单里选：

- Android Chrome：「安装应用 / 添加到主屏幕」
- iPhone Safari：「分享 → 添加到主屏幕」

桌面会出现「无界」图标，全屏运行，和 App 基本一样。
前提是站点用 **HTTPS**（Cloudflare 隧道或域名），局域网 http 只能当普通网页。

### 方式二：打包 APK（本工程）

适合需要真正 APK、可以发给别人安装的场景。

## 打包步骤

1. 安装 [Android Studio](https://developer.android.com/studio)（自带 JDK 和 SDK）；
2. 打开 Android Studio → **Open** → 选择本目录下的 `android` 文件夹；
3. 等待 Gradle 同步完成（首次会下载依赖）；
4. 修改站点地址：`android/app/src/main/res/values/strings.xml` 里的 `site_url`：

```xml
<string name="site_url">https://wujie.你的域名.com</string>
```

局域网调试可以先用 `http://192.168.31.179:8080`。

5. 手机连 USB 开调试 → 点 Run；或菜单 **Build → Build Bundle(s)/APK(s) → Build APK(s)**；
6. 生成的 APK 在 `android/app/build/outputs/apk/debug/`。

正式发布用 **Build → Generate Signed Bundle / APK**，创建自己的签名证书。

## 说明

- App 只是 WebView，播放、登录、历史都走网站本身；
- 支持全屏视频、返回键回退、DOM Storage（登录 Cookie 可保存）；
- 站点改成 HTTPS 后，App 里也建议用 https 地址，避免明文密码；
- 如果网址换了，改 `strings.xml` 重新打包即可。

## 目录

```
wujie-app/
├── README.md
└── android/
    ├── settings.gradle
    ├── build.gradle
    ├── gradle.properties
    └── app/
        ├── build.gradle
        └── src/main/
            ├── AndroidManifest.xml
            ├── java/com/wujie/app/MainActivity.kt
            └── res/values/strings.xml
```
