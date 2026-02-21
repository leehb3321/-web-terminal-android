package com.webterminal.app

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager

class TerminalActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private var serverUrl: String? = null
    private var sessionCookie: String? = null
    private var currentUrl: String? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        serverUrl = intent.getStringExtra("server_url")
        sessionCookie = intent.getStringExtra("session_cookie")

        if (serverUrl == null) {
            finish()
            return
        }

        // Create layout programmatically
        val layout = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        webView = WebView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            id = View.generateViewId()
        }

        layout.addView(webView)
        setContentView(layout)

        // Setup WebView
        setupWebView()

        // Load terminal page
        val terminalUrl = "$serverUrl/"
        currentUrl = terminalUrl

        // Set cookie before loading
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(webView, true)
        if (!sessionCookie.isNullOrEmpty()) {
            val cookieDomain = serverUrl!!.replace("http://", "").replace("https://", "").split(":")[0]
            cookieManager.setCookie(cookieDomain, sessionCookie)
        }

        webView.loadUrl(terminalUrl)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            allowFileAccess = false
            allowContentAccess = false
            loadsImagesAutomatically = true
            blockNetworkImage = false
            useWideViewPort = true
            loadWithOverviewMode = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            mediaPlaybackRequiresUserGesture = false
            javaScriptCanOpenWindowsAutomatically = false
            setSupportMultipleWindows(false)
        }

        webView.addJavascriptInterface(WebAppInterface(this), "Android")

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url.toString()
                // Only allow URLs from the configured server
                if (url.startsWith(serverUrl!!)) {
                    return false
                }
                // Block external URLs
                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // Inject helper script for mobile
                injectMobileHelper()
            }

            override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                super.onReceivedError(view, errorCode, description, failingUrl)
                if (failingUrl == currentUrl) {
                    Toast.makeText(this@TerminalActivity, "Connection error: $description", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Enable debug mode in debug builds
        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)
    }

    private fun injectMobileHelper() {
        webView.evaluateJavascript("""
            (function() {
                // Add mobile-specific styles
                var style = document.createElement('style');
                style.textContent = `
                    body { -webkit-overflow-scrolling: touch; }
                    .mobile-input-helper { display: block !important; }
                    .terminal-wrapper { padding-bottom: 120px !important; }
                `;
                document.head.appendChild(style);

                // Mark as mobile app
                window.isAndroidApp = true;

                // Notify app that page is ready
                if (window.Android) {
                    Android.onPageReady();
                }
            })();
        """, null)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.terminal_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_refresh -> {
                webView.reload()
                true
            }
            R.id.action_fullscreen -> {
                toggleFullscreen()
                true
            }
            R.id.action_logout -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private var isFullscreen = false

    private fun toggleFullscreen() {
        isFullscreen = !isFullscreen
        if (isFullscreen) {
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            )
        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
    }

    private fun logout() {
        webView.evaluateJavascript("""
            (function() {
                fetch('/api/logout', { method: 'POST' })
                    .then(() => window.Android.onLogout())
                    .catch(() => window.Android.onLogout());
            })();
        """, null)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        webView.apply {
            stopLoading()
            settings.javaScriptEnabled = false
            removeJavascriptInterface("Android")
            clearHistory()
            clearCache(true)
            loadUrl("about:blank")
            onPause()
            removeAllViews()
            destroy()
        }
        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        webView.onPause()
    }

    override fun onResume() {
        webView.onResume()
        super.onResume()
    }

    // JavaScript Interface
    class WebAppInterface(private val activity: TerminalActivity) {
        @JavascriptInterface
        fun onPageReady() {
            activity.runOnUiThread {
                // Page is ready
            }
        }

        @JavascriptInterface
        fun onLogout() {
            activity.runOnUiThread {
                // Clear saved password on logout
                val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
                prefs.edit().remove("password").apply()

                activity.finish()
            }
        }

        @JavascriptInterface
        fun showToast(message: String) {
            activity.runOnUiThread {
                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
