package com.sultanagung1.sista.ui.portal

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.sultanagung1.sista.core.designsystem.*
import com.sultanagung1.sista.core.storage.SessionManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SsoWebViewScreen(
    url: String = "https://sista.sultanagung1.sch.id",
    title: String = "Portal Terpadu SMA SA 1",
    sessionManager: SessionManager,
    onNavigateBack: () -> Unit
) {
    var webView: WebView? by remember { mutableStateOf(null) }
    var pageTitle by remember { mutableStateOf(title) }
    var progress by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }

    val userToken = remember {
        runBlocking { sessionManager.authTokenFlow.firstOrNull() ?: "" }
    }

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = pageTitle,
                subtitle = "Single Sign-On (SSO) Terverifikasi",
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (isLoading && progress < 100) {
                LinearProgressIndicator(
                    progress = { progress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = Emerald700
                )
            }

            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.loadWithOverviewMode = true
                        settings.useWideViewPort = true
                        settings.allowFileAccess = false
                        settings.allowContentAccess = false
                        setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)

                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                progress = newProgress
                                isLoading = newProgress < 100
                            }

                            override fun onReceivedTitle(view: WebView?, newTitle: String?) {
                                if (!newTitle.isNullOrEmpty()) {
                                    pageTitle = newTitle
                                }
                            }
                        }

                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                isLoading = true
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                isLoading = false
                            }

                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean {
                                val reqUri = request?.url ?: return false
                                val host = reqUri.host?.lowercase() ?: ""
                                val isInternalHost = host == "sista.sultanagung1.sch.id" ||
                                        host == "api.sultanagung1.sch.id" ||
                                        host == "192.168.31.127" ||
                                        host == "10.0.2.2" ||
                                        host == "localhost" ||
                                        host == "127.0.0.1"

                                if (isInternalHost) {
                                    return false
                                }

                                return try {
                                    val intent = Intent(Intent.ACTION_VIEW, reqUri)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(intent)
                                    true
                                } catch (_: Exception) {
                                    true
                                }
                            }
                        }

                        // Load URL with SSO Authorization header
                        val headers = mutableMapOf<String, String>()
                        if (userToken.isNotEmpty()) {
                            headers["Authorization"] = "Bearer $userToken"
                        }
                        headers["X-Client-Platform"] = "Sulaone-Android-SuperApp"

                        loadUrl(url, headers)
                        webView = this
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
