package com.EGomez.idleecosystem.ui

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * El juego no usa emojis (v0.19.10): cambian de aspecto según el teléfono y rompen la
 * estética. Se dibujan con GameIcons o con la ilustración de cada criatura. Los signos de
 * texto (★ ✦ ◎ ·) sí se permiten.
 */
class NoEmojiTest {

    /** Signos que Android pinta como emoji aunque estén en el plano básico. */
    private val emojiInBmp = setOf(
        0x2620, 0x2694, 0x2697, 0x2699, 0x26A0, 0x26A1, 0x26D4, 0x2734, 0x25B6, 0x2B50, 0x2B55,
        0x2764, 0x2705, 0x274C, 0x2728, 0x231B, 0x23F3, 0x2615, 0x2614, 0x26C4, 0x2708, 0x2709, 0xFE0F
    )

    private fun isEmoji(cp: Int) = cp >= 0x1F000 || cp in emojiInBmp

    @Test
    fun `no source file or string resource contains emojis`() {
        val root = listOf(File("src/main"), File("app/src/main")).first { it.exists() }
        val offenders = root.walkTopDown()
            .filter { it.isFile && (it.extension == "kt" || it.extension == "xml") }
            .flatMap { file ->
                file.readLines().withIndex().mapNotNull { (i, line) ->
                    val bad = line.codePoints().toArray().filter(::isEmoji)
                    if (bad.isEmpty()) null
                    else "${file.name}:${i + 1} " + bad.joinToString { String(Character.toChars(it)) }
                }
            }
            .toList()
        assertTrue("Emojis encontrados:\n" + offenders.joinToString("\n"), offenders.isEmpty())
    }
}
