package advent.of.code.day18

import advent.of.code.day18.Acre.*
import java.io.File

data class Position(val x: Int, val y: Int) : Comparable<Position> {
    override fun compareTo(other: Position): Int {
        return compareBy<Position>({ it.y }, { it.x }).compare(this, other)
    }

    override fun toString(): String {
        return "(${x.toString().padStart(3, ' ')},${y.toString().padStart(3, ' ')})"
    }

    fun up() = copy(y = y - 1)
    fun down() = copy(y = y + 1)
    fun left() = copy(x = x - 1)
    fun right() = copy(x = x + 1)
    fun upLeft() = copy(x = x - 1, y = y - 1)
    fun upRight() = copy(x = x + 1, y = y - 1)
    fun downLeft() = copy(y = y + 1, x = x - 1)
    fun downRight() = copy(y = y + 1, x = x + 1)

    fun around() = listOf(up(), down(), left(), right(), upLeft(), upRight(), downLeft(), downRight())
}

val input = File("day18.txt")

enum class Acre(val symbol: Char) {
    TREES('|'), LUMBERYARD('#'), OPEN('.')
}

class ForestField(input: File) {
    var acres = mapOf<Position, Acre>()

    init {
        var y = 0
        acres = input.readLines().flatMap { line ->
            var x = 0
            line.mapNotNull { symbol ->
                val pos = Position(x++, y)
                when (symbol) {
                    TREES.symbol -> Pair(pos, TREES)
                    LUMBERYARD.symbol -> Pair(pos, LUMBERYARD)
                    else -> null
                }
            }.apply { y++ }
        }.toMap()
    }

    fun around(position: Position) = position.around().mapNotNull { acres[it] }

    fun plots() = (0..(acres.map { it.key.y }.max() ?: 0)).flatMap { y ->
        (0..(acres.map { it.key.x }.max() ?: 0)).map { x ->
            val pos = Position(x, y)
            Pair(pos, acres[pos] ?: OPEN)
        }
    }


    override fun toString(): String {
        var output = ""
        for (y in (0..(acres.map { it.key.y }.max() ?: 0))) {
            for (x in (0..(acres.map { it.key.x }.max() ?: 0)))
                output += (acres[Position(x, y)] ?: OPEN).symbol
            output += '\n'
        }
        return output
    }
}

// 1000000000

fun part1(): Int {
    val forest = ForestField(input)
    println((1..1000000000).map { idx ->
        forest.acres = forest.plots().mapNotNull {
            when (it.second) {
                TREES -> {
                    if (forest.around(it.first).count { near -> near == LUMBERYARD } >= 3)
                        Pair(it.first, LUMBERYARD)
                    else
                        it
                }
                LUMBERYARD -> {
                    if (forest.around(it.first).count { near -> near == LUMBERYARD } >= 1 &&
                        forest.around(it.first).count { near -> near == TREES } >= 1)
                        Pair(it.first, LUMBERYARD)
                    else
                        null
                }
                else -> {
                    if (forest.around(it.first).count { near -> near == TREES } >= 3)
                        Pair(it.first, TREES)
                    else
                        it
                }
            }
        }.toMap()
        val wood = forest.plots().count { it.second == TREES }
        val yards = forest.plots().count { it.second == LUMBERYARD }
        if (((idx - 1000) % 7000 in listOf(6999, 0, 1))) println("$idx -> $wood -> $yards -> ${wood * yards}")
        idx to wood * yards
    }.last())
    return forest.plots().count { it.second == LUMBERYARD } * forest.plots().count { it.second == TREES }
}