package advent.of.code.day10

import java.io.File

private val lineRegex = Regex("position=<([- ]?[0-9]+), ([- ]?[0-9]+)> velocity=<([- ]?[0-9]+), ([- ]?[0-9]+)>")

private var input = File("day10.txt").readLines().map {
    val res = lineRegex.find(it.trim())
    listOf(
        res?.groupValues?.get(1)?.trim()?.toInt() ?: 0,
        res?.groupValues?.get(2)?.trim()?.toInt() ?: 0,
        res?.groupValues?.get(3)?.trim()?.toInt() ?: 0,
        res?.groupValues?.get(4)?.trim()?.toInt() ?: 0
    )
}.toList()


fun List<List<Int>>.move() = map { listOf(it[0] + it[2], it[1] + it[3], it[2], it[3]) }.toList()

fun List<List<Int>>.height() = map { it[1] }.max()!! - map { it[1] }.min()!!

fun part1() {
    var result = input.toList()
    var before = input.toList()
    var min = Int.MAX_VALUE
    for (s in (0..10000000)) {
        val current = result.height()
        if (current > min) {
            var msg = "\n"
            val minX = before.map { it[0] }.min()!!
            val maxX = before.map { it[0] }.max()!!
            val minY = before.map { it[1] }.min()!!
            val maxY = before.map { it[1] }.max()!!
            for (y in (minY..maxY)) {
                for (x in (minX..maxX))
                    msg += if (before.any { it[0] == x && it[1] == y }) '#' else ' '
                msg += '\n'
            }
            println(s - 1)
            println(msg)
            break
        }
        min = current
        before = result
        result = result.move()
    }
}

