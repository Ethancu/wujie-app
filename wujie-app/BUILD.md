# 打包 APK

两条路，任选。

## 路线一：GitHub 自动打包（不用装 Android Studio）

1. 在 GitHub 新建一个仓库（私有也可以）；
2. 把当前工作区里的这两部分推上去：

```
wujie-app/                 ← App 工程
.github/workflows/build-apk.yml   ← 自动构建配置
```

3. 打开仓库的 **Actions** 标签，选「Build Wujie APK」→ **Run workflow**；
4. 等 3～5 分钟，进入这次运行，页面底部 **Artifacts** 里下载 `wujie-apk.zip`；
5. 解压得到 `app-debug.apk`，传到手机安装（需允许“安装未知来源应用”）。

> 改了 `strings.xml` 里的站点地址后，重新跑一次 workflow 即可。

## 路线二：本地 Android Studio

1. 安装 Android Studio；
2. Open → 选 `wujie-app/android`；
3. 改 `app/src/main/res/values/strings.xml`：

```xml
<string name="site_url">https://wujie.你的域名.com</string>
```

4. Run 装到手机，或 Build → Build APK(s)。

## 站点地址

App 只是 WebView，加载的就是 `site_url`：

- 外网：`https://wujie.你的域名.com`（推荐，登录密码不会明文）
- 局域网调试：`http://192.168.31.179:8080`

## 安装后

- 打开即全屏显示站点；
- 返回键会先回退网页，退到底再退出 App；
- 登录状态、观看历史都保存在网页端（Cookie），App 不额外存储。

## 发布给别人的注意事项

- debug APK 可以直接装，但正式分发建议生成签名版（Android Studio：Build → Generate Signed Bundle / APK）；
- 如果网站没有 HTTPS，别人安装后访问会提示明文风险，建议先配 Cloudflare 隧道。
