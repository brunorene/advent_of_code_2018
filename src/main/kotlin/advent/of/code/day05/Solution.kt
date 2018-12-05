package advent.of.code.day05

import java.io.File

private val input = File("day05.txt").readText().trim()

private val regex = Regex(('a'..'z').map { "$it${it.toUpperCase()}|${it.toUpperCase()}$it" }.joinToString("|"))

fun shrink(source: String): Int {
    var str = source
    do {
        val len = str.length
        str = str.replace(regex, "")
    } while (len != str.length)
    return str.length
}

fun part1() = shrink(input)

fun part2(): Int {
    var min = Int.MAX_VALUE
    for (removeC in 'a'..'z') {
        var str = input
        str = str.replace(Regex("[$removeC${removeC.toUpperCase()}]"), "")
        min = arrayOf(min, shrink(str)).min()!!
    }
    return min
}

