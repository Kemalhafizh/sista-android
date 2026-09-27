package com.sultanagung1.sista.widget

import com.sultanagung1.sista.R
import java.io.File
import java.util.Locale
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Renders [WidgetText] the way the widget would, from the real
 * res/values…/strings.xml, so the tests assert on the text a user sees.
 */
object WidgetTestStrings {

    fun resDir(): File =
        listOf(File("src/main/res"), File("app/src/main/res")).firstOrNull { it.isDirectory }
            ?: error("res directory not found")

    fun strings(valuesDir: String = "values"): Map<String, String> {
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            .parse(File(resDir(), "$valuesDir/strings.xml"))
        val nodes = doc.getElementsByTagName("string")
        return (0 until nodes.length).associate { i ->
            val node = nodes.item(i)
            node.attributes.getNamedItem("name").nodeValue to unescape(node.textContent)
        }
    }

    private val defaultStrings by lazy { strings() }

    private val names: Map<Int, String> by lazy {
        R.string::class.java.fields.associate { it.getInt(null) to it.name }
    }

    fun render(text: WidgetText?): String? = when (text) {
        null -> null
        is WidgetText.Raw -> text.text
        is WidgetText.Res -> {
            val name = names[text.id] ?: error("unknown string id ${text.id}")
            val template = defaultStrings[name] ?: error("string $name missing from values/strings.xml")
            val args = text.args.map { if (it is WidgetText) render(it) else it }.toTypedArray()
            String.format(Locale.ROOT, template, *args)
        }
    }

    /** Android string-resource escapes used in this project. */
    private fun unescape(raw: String): String =
        raw.replace("\\'", "'").replace("\\\"", "\"").replace("\\n", "\n")
}
