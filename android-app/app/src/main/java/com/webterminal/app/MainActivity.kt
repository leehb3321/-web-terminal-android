package com.webterminal.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.webterminal.app.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import java.net.HttpURLConnection

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load saved server address
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        binding.etServerAddress.setText(prefs.getString("server_address", ""))
        binding.etPassword.setText(prefs.getString("password", ""))
        binding.switchRemember.isChecked = prefs.getBoolean("remember_password", false)

        // Auto-connect if credentials are saved
        if (binding.etServerAddress.text.isNotEmpty() && binding.etPassword.text.isNotEmpty()) {
            binding.etServerAddress.postDelayed({
                if (prefs.getBoolean("auto_connect", false)) {
                    attemptLogin()
                }
            }, 500)
        }

        binding.btnConnect.setOnClickListener {
            attemptLogin()
        }

        binding.etPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO) {
                attemptLogin()
                true
            } else {
                false
            }
        }
    }

    private fun attemptLogin() {
        val serverAddress = binding.etServerAddress.text.toString().trim()
        val password = binding.etPassword.text.toString()

        if (serverAddress.isEmpty()) {
            binding.etServerAddress.error = "Please enter server address"
            return
        }

        if (password.isEmpty()) {
            binding.etPassword.error = "Please enter password"
            return
        }

        // Save preferences if remember is checked
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        prefs.edit().apply {
            putString("server_address", serverAddress)
            if (binding.switchRemember.isChecked) {
                putString("password", password)
                putBoolean("remember_password", true)
            } else {
                remove("password")
                putBoolean("remember_password", false)
            }
            apply()
        }

        // Normalize server address
        var serverUrl = serverAddress
        if (!serverUrl.startsWith("http://") && !serverUrl.startsWith("https://")) {
            serverUrl = "http://$serverUrl"
        }
        serverUrl = serverUrl.trimEnd('/')

        setLoading(true)

        // Perform login in background
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = performLogin(serverUrl, password)
                withContext(Dispatchers.Main) {
                    setLoading(false)
                    if (result.first) {
                        // Login successful, start terminal activity
                        val intent = Intent(this@MainActivity, TerminalActivity::class.java).apply {
                            putExtra("server_url", serverUrl)
                            putExtra("session_cookie", result.second)
                        }
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@MainActivity, result.second, Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    setLoading(false)
                    Toast.makeText(this@MainActivity, "Connection failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun performLogin(serverUrl: String, password: String): Pair<Boolean, String> {
        val url = URL("$serverUrl/api/login")
        val connection = (if (serverUrl.startsWith("https")) url.openConnection() as HttpsURLConnection
                         else url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            doOutput = true
            connectTimeout = 10000
            readTimeout = 10000
        }

        try {
            val requestBody = JSONObject().apply {
                put("password", password)
            }.toString()

            connection.outputStream.use { os ->
                val input = requestBody.toByteArray(Charsets.UTF_8)
                os.write(input, 0, input.size)
            }

            val responseCode = connection.responseCode
            val cookieHeader = connection.headerFields["Set-Cookie"]
            val sessionCookie = cookieHeader?.firstOrNull { it.contains("webterminal.sid") } ?: ""

            if (responseCode == 200) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                if (json.optBoolean("success", false)) {
                    return Pair(true, sessionCookie)
                }
                return Pair(false, json.optString("error", "Login failed"))
            } else if (responseCode == 429) {
                return Pair(false, "Too many attempts. Please wait and try again.")
            } else {
                return Pair(false, "Login failed (HTTP $responseCode)")
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.btnConnect.isEnabled = !loading
        binding.etServerAddress.isEnabled = !loading
        binding.etPassword.isEnabled = !loading
        binding.progressBar.visible = loading
        binding.btnConnect.text = if (loading) "Connecting..." else "Connect"
    }

    private var <T> T.visible: T
        get() = this
        set(value) {
            if (this is android.view.View) {
                visibility = if (value is Boolean && value) android.view.View.VISIBLE else android.view.View.GONE
            }
        }
}
