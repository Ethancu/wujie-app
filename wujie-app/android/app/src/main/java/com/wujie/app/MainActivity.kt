package com.wujie.app

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class MainActivity : AppCompatActivity() {

    private lateinit var web: WebView
    private lateinit var gear: TextView
    private val prefs by lazy { getSharedPreferences("wujie", Context.MODE_PRIVATE) }
    private val handler = Handler(Looper.getMainLooper())
    private var pageLoaded = false
    private var dialogShowing = false

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    inner class UiBridge {
        @JavascriptInterface
        fun setFullscreen(on: Boolean) {
            runOnUiThread {
                gear.visibility = if (on) View.GONE else View.VISIBLE
                val controller = WindowInsetsControllerCompat(window, window.decorView)
                if (on) {
                    WindowCompat.setDecorFitsSystemWindows(window, false)
                    controller.hide(WindowInsetsCompat.Type.systemBars())
                    controller.systemBarsBehavior =
                        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                } else {
                    WindowCompat.setDecorFitsSystemWindows(window, true)
                    controller.show(WindowInsetsCompat.Type.systemBars())
                }
            }
        }
    }

    private fun savedUrl(): String =
        prefs.getString("site_url", "")?.takeIf { it.isNotBlank() }
            ?: getString(R.string.site_url)

    private fun normalize(raw: String): String {
        var u = raw.trim()
        if (u.isEmpty()) return ""
        if (!u.startsWith("http://") && !u.startsWith("https://")) u = "https://$u"
        return u.trimEnd('/')
    }

    private fun load(url: String) {
        pageLoaded = false
        web.loadUrl(url)
        handler.postDelayed({
            if (!pageLoaded && !isFinishing && !dialogShowing) {
                askUrl("这个地址打不开，请检查或换一个")
            }
        }, 15000)
    }

    private fun askUrl(reason: String) {
        if (dialogShowing || isFinishing) return
        dialogShowing = true

        val input = EditText(this).apply {
            setText(savedUrl())
            inputType = InputType.TYPE_TEXT_VARIATION_URI
            setSingleLine(true)
            setSelection(text.length)
        }
        AlertDialog.Builder(this)
            .setTitle("修改服务器地址")
            .setMessage(reason)
            .setView(input)
            .setCancelable(false)
            .setPositiveButton("保存并重试") { _, _ ->
                dialogShowing = false
                val url = normalize(input.text.toString())
                if (url.isEmpty()) {
                    Toast.makeText(this, "地址不能为空", Toast.LENGTH_SHORT).show()
                    askUrl(reason)
                    return@setPositiveButton
                }
                prefs.edit().putString("site_url", url).apply()
                load(url)
            }
            .setNegativeButton("退出") { _, _ ->
                dialogShowing = false
                finish()
            }
            .show()
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = FrameLayout(this)
        web = WebView(this)
        root.addView(web, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))

        gear = TextView(this).apply {
            text = "⚙"
            setTextColor(Color.parseColor("#CCFFFFFF"))
            textSize = 20f
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#66000000"))
            setOnClickListener { askUrl("修改服务器地址") }
        }
        val lp = FrameLayout.LayoutParams(dp(40), dp(40), Gravity.BOTTOM or Gravity.END)
        lp.setMargins(0, 0, dp(12), dp(72))
        root.addView(gear, lp)
        setContentView(root)

        web.addJavascriptInterface(UiBridge(), "WujieUI")
        web.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            mediaPlaybackRequiresUserGesture = false
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            useWideViewPort = true
            loadWithOverviewMode = true
            cacheMode = WebSettings.LOAD_DEFAULT
        }
        web.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                pageLoaded = true
            }

            override fun onReceivedError(
                view: WebView?, request: WebResourceRequest?, error: WebResourceError?
            ) {
                if (request?.isForMainFrame == true) {
                    askUrl("这个地址打不开，请检查网络或换一个地址")
                }
            }

            override fun onReceivedHttpError(
                view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?
            ) {
                if (request?.isForMainFrame == true && (errorResponse?.statusCode ?: 0) >= 500) {
                    askUrl("服务器返回错误（${errorResponse?.statusCode}），请检查地址")
                }
            }
        }
        web.webChromeClient = WebChromeClient()

        load(savedUrl())
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && this::web.isInitialized && web.canGoBack()) {
            web.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onPause() {
        super.onPause()
        if (this::web.isInitialized) web.onPause()
    }

    override fun onResume() {
        super.onResume()
        if (this::web.isInitialized) web.onResume()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        if (this::web.isInitialized) web.destroy()
        super.onDestroy()
    }
}
