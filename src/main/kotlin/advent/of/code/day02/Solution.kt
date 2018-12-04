package advent.of.code.day02

import java.io.File

fun part1(): Int {
    var threesCount = 0
    var twosCount = 0
    File("day02.txt")
        .readLines()
        .forEach {
            val counts = it.groupBy { c -> c }
            if (counts.any { e -> e.value.size == 2 })
                twosCount++
            if (counts.any { e -> e.value.size == 3 })
                threesCount++
        }
    return twosCount * threesCount
}

fun part2(): String {
    var result = ""
    File("day02.txt")
        .readLines()
        .forEach { str1 ->
            File("day02.txt")
                .readLines()
                .forEach { str2 ->
                    val seq = str1.asSequence().zip(str2.asSequence())
                    if (seq.filter { (a, b) -> a != b }.count() == 1)
                        result = seq.filter { (a, b) -> a == b }.map { (a, _) -> a }.joinToString("")
                }
        }
    return result
}