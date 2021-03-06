package advent.of.code.day05

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.io.File

private val input = File("day05.txt").readText().trim()

private val regex = Regex(('a'..'z').joinToString("|") { "$it${it.toUpperCase()}|${it.toUpperCase()}$it" })

fun shrink(source: String): Int {
    var str = source
    do {
        val len = str.length
        str = str.replace(regex, "")
    } while (len != str.length)
    return str.length
}

fun part1() = shrink(input)

suspend fun part2() = ('a'..'z').map {
    GlobalScope.async {
        var str = input
        str = str.replace(Regex("[$it${it.toUpperCase()}]"), "")
        shrink(str)
    }
}.map { it.await() }.min()

