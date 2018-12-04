package advent.of.code.day04

import advent.of.code.day04.State.*
import java.io.File

enum class State(val match: Regex) {
    SHIFT(Regex("\\[([0-9]{4})-([0-9]{2})-([0-9]{2}) ([0-9]{2}):([0-9]{2})\\] Guard #([0-9]+) begins shift")),
    SLEEP(Regex("\\[([0-9]{4})-([0-9]{2})-([0-9]{2}) ([0-9]{2}):([0-9]{2})\\] falls asleep")),
    AWAKE(Regex("\\[([0-9]{4})-([0-9]{2})-([0-9]{2}) ([0-9]{2}):([0-9]{2})\\] wakes up"))
}

fun commonPart(selector: (Map.Entry<Int, Array<Int>>) -> Int): Int {
    val guardSleep = mutableMapOf<Int, Array<Int>>()
    var currentGuard = 0
    var asleep = 0
    val lines = File("day04.txt").readLines().sorted()
    for (line in lines) {
        when {
            SHIFT.match matches line -> {
                currentGuard = SHIFT.match.find(line)?.groups!![6]!!.value.toInt()
                if (guardSleep[currentGuard] == null)
                    guardSleep[currentGuard] = Array(60) { 0 }
            }
            SLEEP.match matches line -> {
                asleep = SLEEP.match.find(line)!!.groups[5]!!.value.toInt()
            }
            AWAKE.match matches line -> {
                val awake = AWAKE.match.find(line)!!.groups[5]!!.value.toInt()
                for (b in (asleep until awake))
                    guardSleep[currentGuard]!![b]++
            }
        }
    }
    val guardMostSleep = guardSleep.maxBy(selector)!!.key
    return guardMostSleep * guardSleep[guardMostSleep]!!
        .mapIndexed { index, i -> Pair(index, i) }
        .maxBy { it.second }!!.first

}

fun part1() = commonPart { it.value.sum() }

fun part2() = commonPart { it.value.max() ?: 0 }