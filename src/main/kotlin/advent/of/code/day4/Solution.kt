package advent.of.code.day4

import advent.of.code.day4.State.*
import java.io.File

enum class State(val match: Regex) {
    SHIFT(Regex("\\[([0-9]{4})-([0-9]{2})-([0-9]{2}) ([0-9]{2}):([0-9]{2})\\] Guard #([0-9]+) begins shift")),
    SLEEP(Regex("\\[([0-9]{4})-([0-9]{2})-([0-9]{2}) ([0-9]{2}):([0-9]{2})\\] falls asleep")),
    AWAKE(Regex("\\[([0-9]{4})-([0-9]{2})-([0-9]{2}) ([0-9]{2}):([0-9]{2})\\] wakes up"))
}


fun part1(): Int {
    val guardSleep = mutableMapOf<Int, Array<Int>>()
    var currentGuard = 0
    var asleep = 0
    val lines = File("day4.txt").readLines().sorted()
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
    val guardMostSleep = guardSleep.maxBy { it.value.sum() }!!.key
    return guardMostSleep * guardSleep[guardMostSleep]!!
        .mapIndexed { index, i -> Pair(index, i) }
        .maxBy { it.second }!!.first
}

fun part2() {
}