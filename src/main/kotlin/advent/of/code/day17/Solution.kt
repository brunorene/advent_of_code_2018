package advent.of.code.day17

import java.io.File
import java.util.*

class Position(name1: String, val1: Int, val2: Int) : Comparable<Position> {
    val x: Int
    val y: Int

    init {
        if (name1 == "x") {
            x = val1
            y = val2
        } else {
            x = val2
            y = val1
        }
    }

    override fun compareTo(other: Position): Int {
        return compareBy<Position>({ it.y }, { it.x }).compare(this, other)
    }

    override fun toString(): String {
        return "(${x.toString().padStart(3, ' ')},${y.toString().padStart(3, ' ')})"
    }
}

val input = File("day17.txt")

val multipleCoord = Regex("(x|y)=([.0-9]+)\\.\\.([.0-9]+)")
val oneCoord = Regex("(x|y)=([.0-9]+)")

class Underground(input: File) {
    val clay: MutableSet<Position> = TreeSet()

    init {
        input.readLines().forEach { line ->
            val parts = line.split(",").map { it.trim() }
            var multiResult = multipleCoord.find(parts[0])
            if (multiResult != null) {
                val coordName1 = multiResult.groupValues[1]
                val startCoord1 = multiResult.groupValues[2].toInt()
                val endCoord1 = multiResult.groupValues[3].toInt()
                val oneResult = oneCoord.find(parts[1])
                val startCoord2 = oneResult!!.groupValues[2].toInt()
                for (c in (startCoord1..endCoord1))
                    clay.add(Position(coordName1, c, startCoord2))
            } else {
                val oneResult = oneCoord.find(parts[0])
                val coordName1 = oneResult!!.groupValues[1]
                val startCoord1 = oneResult.groupValues[2].toInt()
                multiResult = multipleCoord.find(parts[1])
                val startCoord2 = multiResult!!.groupValues[2].toInt()
                val endCoord2 = multiResult.groupValues[3].toInt()
                for (c in (startCoord2..endCoord2))
                    clay.add(Position(coordName1, startCoord1, c))
            }
        }
    }
}

fun part1(): Int {
    val underground = Underground(input)
    println(underground.clay)
    return 1
}

fun part2() = 2