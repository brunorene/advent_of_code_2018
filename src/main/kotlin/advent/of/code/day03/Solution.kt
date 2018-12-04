package advent.of.code.day03

import java.io.File

fun part1(): Int {
    val fabric = List(1000) { MutableList(1000) { 0 } }
    File("day03.txt").readLines().forEach { line ->
        val regex = Regex("#[0-9]+ @ ([0-9]+),([0-9]+): ([0-9]+)x([0-9]+)")
        val result = regex.find(line)
        if (result != null) {
            val left = result.groups[1]?.value?.toInt() ?: 0
            val top = result.groups[2]?.value?.toInt() ?: 0
            val width = result.groups[3]?.value?.toInt() ?: 0
            val height = result.groups[4]?.value?.toInt() ?: 0
            (left until (left + width)).forEach { row ->
                (top until (top + height)).forEach { col ->
                    fabric[row][col]++
                }
            }
        }
    }
    var overlap = 0
    fabric.forEach { line ->
        overlap += line.filter { it > 1 }.count()
    }
    return overlap
}

fun part2(): Int {
    val fabric = List(1000) { MutableList(1000) { 0 } }
    File("day03.txt").readLines().forEach { line ->
        val regex = Regex("#[0-9]+ @ ([0-9]+),([0-9]+): ([0-9]+)x([0-9]+)")
        val result = regex.find(line)
        if (result != null) {
            val left = result.groups[1]?.value?.toInt() ?: 0
            val top = result.groups[2]?.value?.toInt() ?: 0
            val width = result.groups[3]?.value?.toInt() ?: 0
            val height = result.groups[4]?.value?.toInt() ?: 0
            (left until (left + width)).forEach { row ->
                (top until (top + height)).forEach { col ->
                    fabric[row][col]++
                }
            }
        }
    }
    var goodId = -1
    File("day03.txt").readLines().forEach { line ->
        val regex = Regex("#([0-9]+) @ ([0-9]+),([0-9]+): ([0-9]+)x([0-9]+)")
        val result = regex.find(line)
        if (result != null) {
            val id = result.groups[1]?.value?.toInt() ?: 0
            val left = result.groups[2]?.value?.toInt() ?: 0
            val top = result.groups[3]?.value?.toInt() ?: 0
            val width = result.groups[4]?.value?.toInt() ?: 0
            val height = result.groups[5]?.value?.toInt() ?: 0
            var overlap = false
            for (row in (left until (left + width))) {
                for (col in (top until (top + height))) {
                    if (fabric[row][col] > 1) {
                        overlap = true
                        break
                    }
                }
                if (overlap)
                    break
            }
            if (!overlap)
                goodId = id
        }
    }
    return goodId
}