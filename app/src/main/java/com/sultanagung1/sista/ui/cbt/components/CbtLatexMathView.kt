package com.sultanagung1.sista.ui.cbt.components

import android.annotation.SuppressLint
import android.graphics.Color as AndroidColor
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.viewinterop.AndroidView

/**
 * CbtLatexMathView
 *
 * Komponen cerdas untuk menampilkan teks soal dan pilihan jawaban CBT SMA Islam Sultan Agung 1.
 * - Mendeteksi secara otomatis formula saintek LaTeX ($...$, $$...$$, \frac, \sqrt, \int, dsb.)
 * - Jika teks biasa: di-render dengan native Compose Text untuk performa maksimal 120fps.
 * - Jika memuat LaTeX: di-render menggunakan KaTeX Engine dengan latar belakang transparan
 *   dan kontras warna adaptif otomatis (Light / Dark Mode).
 */
@Composable
fun CbtLatexMathView(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = MaterialTheme.colorScheme.onSurface,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    if (text.isBlank()) return

    val containsLatex = remember(text) {
        text.contains("$") ||
                text.contains("\\(") ||
                text.contains("\\[") ||
                text.contains("\\frac") ||
                text.contains("\\sqrt") ||
                text.contains("\\int") ||
                text.contains("\\sum") ||
                text.contains("\\lim") ||
                text.contains("\\begin{") ||
                text.contains("\\alpha") ||
                text.contains("\\beta") ||
                text.contains("\\theta") ||
                text.contains("\\lambda") ||
                text.contains("\\rightarrow")
    }

    if (!containsLatex) {
        // High-performance direct Compose text rendering
        Text(
            text = text,
            style = style,
            color = color,
            modifier = modifier
        )
    } else {
        // KaTeX WebView Renderer
        KaTeXWebView(
            latexText = text,
            modifier = modifier,
            textColor = color,
            fontSizeSp = if (style.fontSize.isSpecified) style.fontSize.value else 15f,
            isDarkTheme = isDarkTheme
        )
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun KaTeXWebView(
    latexText: String,
    modifier: Modifier = Modifier,
    textColor: Color,
    fontSizeSp: Float,
    isDarkTheme: Boolean
) {
    var webViewHeight by remember { mutableIntStateOf(44) }

    val hexColor = remember(textColor) {
        String.format("#%06X", 0xFFFFFF and textColor.toArgb())
    }

    val base64Latex = remember(latexText) {
        android.util.Base64.encodeToString(latexText.toByteArray(Charsets.UTF_8), android.util.Base64.NO_WRAP)
    }

    val htmlData = remember(base64Latex, hexColor, fontSizeSp, isDarkTheme) {
        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/katex.min.css" crossorigin="anonymous">
            <script defer src="https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/katex.min.js" crossorigin="anonymous"></script>
            <script defer src="https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/contrib/auto-render.min.js" crossorigin="anonymous"></script>
            <style>
                * {
                    margin: 0;
                    padding: 0;
                    box-sizing: border-box;
                    -webkit-tap-highlight-color: transparent;
                }
                body {
                    background-color: transparent;
                    color: $hexColor;
                    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
                    font-size: ${fontSizeSp.toInt()}px;
                    line-height: 1.5;
                    word-break: break-word;
                    overflow: hidden;
                    padding: 2px 0;
                }
                .katex {
                    font-size: 1.12em !important;
                }
                .katex-display {
                    margin: 8px 0 !important;
                    overflow-x: auto;
                    overflow-y: hidden;
                }
            </style>
        </head>
        <body>
            <div id="content"></div>

            <script>
                document.addEventListener("DOMContentLoaded", function() {
                    function decodeBase64(str) {
                        try {
                            return decodeURIComponent(escape(window.atob(str)));
                        } catch (e) {
                            return window.atob(str);
                        }
                    }
                    var rawText = decodeBase64("$base64Latex");
                    var container = document.getElementById('content');
                    container.innerText = rawText;

                    if (window.renderMathInElement) {
                        renderMathInElement(container, {
                            delimiters: [
                                {left: '$$', right: '$$', display: true},
                                {left: '$', right: '$', display: false},
                                {left: '\\(', right: '\\)', display: false},
                                {left: '\\[', right: '\\]', display: true}
                            ],
                            throwOnError: false
                        });
                    }

                    function updateHeight() {
                        var height = document.body.scrollHeight || document.documentElement.scrollHeight;
                        if (window.AndroidInterface && height > 0) {
                            window.AndroidInterface.onHeightChanged(height);
                        }
                    }

                    setTimeout(updateHeight, 80);
                    setTimeout(updateHeight, 300);
                });
            </script>
        </body>
        </html>
        """.trimIndent()
    }

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                setBackgroundColor(AndroidColor.TRANSPARENT)
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = false
                settings.allowFileAccess = false
                settings.allowContentAccess = false
                
                addJavascriptInterface(object {
                    @JavascriptInterface
                    fun onHeightChanged(heightPx: Int) {
                        val density = ctx.resources.displayMetrics.density
                        val heightDp = (heightPx / density).toInt().coerceAtLeast(32)
                        webViewHeight = heightDp
                    }
                }, "AndroidInterface")

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        view?.evaluateJavascript(
                            "(function() { return document.body.scrollHeight; })();"
                        ) { result ->
                            val height = result?.toFloatOrNull()?.toInt()
                            if (height != null && height > 0) {
                                val density = ctx.resources.displayMetrics.density
                                val heightDp = (height / density).toInt().coerceAtLeast(32)
                                webViewHeight = heightDp
                            }
                        }
                    }
                }
                loadDataWithBaseURL("https://cdn.jsdelivr.net", htmlData, "text/html", "UTF-8", null)
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL("https://cdn.jsdelivr.net", htmlData, "text/html", "UTF-8", null)
        },
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 32.dp, max = 800.dp)
            .height(webViewHeight.dp)
    )
}
