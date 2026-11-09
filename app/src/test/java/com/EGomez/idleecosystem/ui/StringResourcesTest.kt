package com.EGomez.idleecosystem.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Cada texto de la interfaz existe en ambos idiomas y con los mismos huecos
 * (%1$s, %2$d...). Un texto que falte en inglés se vería en español; un hueco que
 * falte haría fallar el formato en tiempo de ejecución.
 */
class StringResourcesTest {

    private fun load(folder: String): Map<String, String> {
        val file = File("src/main/res/$folder/strings.xml")
        assertTrue("No existe ${file.absolutePath}", file.exists())
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
        val nodes = doc.getElementsByTagName("string")
        return (0 until nodes.length).associate { i ->
            val node = nodes.item(i)
            node.attributes.getNamedItem("name").nodeValue to node.textContent
        }.filterKeys { it != "app_name" }
    }

    private val placeholder = Regex("%\\d+\\$[sd]")

    @Test
    fun `spanish and english have the same keys`() {
        val es = load("values")
        val en = load("values-en")
        assertEquals("Faltan en inglés", emptySet<String>(), es.keys - en.keys)
        assertEquals("Sobran en inglés", emptySet<String>(), en.keys - es.keys)
    }

    @Test
    fun `translations keep the same placeholders`() {
        val es = load("values")
        val en = load("values-en")
        es.forEach { (key, value) ->
            assertEquals(
                "Los huecos de '$key' no coinciden",
                placeholder.findAll(value).map { it.value }.sorted().toList(),
                placeholder.findAll(en.getValue(key)).map { it.value }.sorted().toList()
            )
        }
    }
}
