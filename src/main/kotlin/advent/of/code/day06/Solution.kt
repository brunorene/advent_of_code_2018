package advent.of.code.day06

import java.io.File
import kotlin.math.abs

data class Point(val x: Int, val y: Int) {

    fun distance(b: Point) = abs(x - b.x) + abs(y - b.y)
}

val input: List<Point> = File("day06.txt").readLines().map {
    val parts = it.split(",")
    Point(parts[0].trim().toInt(), parts[1].trim().toInt())
}

fun part1(): Int {
    val topLeftBig = Point(
        input.map { it.x }.min()!! - 10,
        input.map { it.y }.min()!! - 10
    )
    val topLeft = Point(
        input.map { it.x }.min()!!,
        input.map { it.y }.min()!!
    )
    val bottomRightBig = Point(
        input.map { it.x }.max()!! + 10,
        input.map { it.y }.max()!! + 10
    )
    val bottomRight = Point(
        input.map { it.x }.max()!!,
        input.map { it.y }.max()!!
    )

    val areas = mutableMapOf<Point, Int>()

    for (x in (topLeft.x..bottomRight.x))
        for (y in (topLeft.y..bottomRight.y)) {
            val current = Point(x, y)
            val distances = input.sortedBy { current.distance(it) }.map { Pair(it, it.distance(current)) }
            if (distances[0].second != distances[1].second)
                areas.compute(distances[0].first) { _, a -> (a ?: 0) + 1 }
        }

    val areasBig = mutableMapOf<Point, Int>()

    for (x in (topLeftBig.x..bottomRightBig.x))
        for (y in (topLeftBig.y..bottomRightBig.y)) {
            val current = Point(x, y)
            val distances = input.sortedBy { current.distance(it) }.map { Pair(it, it.distance(current)) }
            if (distances[0].second != distances[1].second)
                areasBig.compute(distances[0].first) { _, a -> (a ?: 0) + 1 }
        }

    return areas.filter { areasBig[it.key] == it.value }.values.max()!!
}

fun part2(): Int {
    val topLeft = Point(
        input.map { it.x }.min()!!,
        input.map { it.y }.min()!!
    )
    val bottomRight = Point(
        input.map { it.x }.max()!!,
        input.map { it.y }.max()!!
    )

    var area = 0

    for (x in (topLeft.x..bottomRight.x))
        for (y in (topLeft.y..bottomRight.y)) {
            val current = Point(x, y)
            val allDists = input.map { it.distance(current) }.sum()
            if (allDists <= 10000)
                area++
        }

    return area
}

