package advent.of.code.day01

import java.io.File

fun part1() = File("day01.txt").readLines().map { it.toInt() }.sum()

fun part2(): Int? {
    val pastFreqs: MutableSet<Int> = mutableSetOf(0)
    var currentFreq = 0
    var found: Int? = null
    while (true) {
        val freqs = File("day01.txt").readLines().map { it.toInt() }
        for (freq in freqs) {
            currentFreq += freq
            if (pastFreqs.contains(currentFreq)) {
                found = currentFreq
                break
            } else
                pastFreqs.add(currentFreq)
        }
        if (found != null)
            break
    }
    return found
}