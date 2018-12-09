package advent.of.code.day09

import java.util.*

private fun rotate(deque: Deque<Int>, count: Int) {
    if (count > 0) {
        for (i in (1..count)) {
            deque.addFirst(deque.removeLast())
        }
    }
    if (count < 0) {
        for (i in (count..-1)) {
            deque.addLast(deque.removeFirst())
        }
    }
}

private fun marbles(highestMarble: Int): Long? {
    val playerCount = 477
    val scores = MutableList(playerCount) { 0L }
    val table = ArrayDeque(listOf(0))
    var currentPlayer = 0
    for (currentMarble in (1..highestMarble)) {
        if (currentMarble % 23 == 0) {
            rotate(table, 7)
            scores[currentPlayer] += currentMarble.toLong() + table.removeFirst().toLong()
        } else {
            rotate(table, -2)
            table.addFirst(currentMarble)
        }
        currentPlayer = (currentPlayer + 1) % playerCount
    }
    return scores.max()
}

fun part1() = marbles(70851)

fun part2() = marbles(7085100)

