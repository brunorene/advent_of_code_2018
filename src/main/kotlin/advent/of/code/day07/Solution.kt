package advent.of.code.day07

import java.io.File
import java.util.*

val lineRegex = Regex("^Step ([A-Z]) must.+step ([A-Z]) can begin.$")

fun part1(): String {
    val dependsOn = mutableMapOf<Char, String>()
    val steps = TreeSet<Char>()
    File("day07.txt").readLines().forEach { line ->
        val res = lineRegex.find(line)
        steps += res!!.groupValues[1][0]
        steps += res!!.groupValues[2][0]
        dependsOn.compute(res!!.groupValues[2][0]) { _, v -> (v ?: "") + res.groupValues[1][0] }
    }
    var stepOrder = ""
    while (steps.isNotEmpty()) {
        val nextSteps = TreeSet<Char>()
        nextSteps.addAll(dependsOn.filter { it.value.isEmpty() }.keys.sorted())
        nextSteps.addAll(steps.filter { !dependsOn.contains(it) })
        val step = nextSteps.first()
        stepOrder += step
        steps.remove(step)
        dependsOn.remove(step)
        dependsOn.forEach { k, v -> dependsOn[k] = v.replace(step.toString(), "") }
    }
    return stepOrder
}

fun part2(): Int {
    val dependsOn = mutableMapOf<Char, String>()
    val steps = TreeSet<Char>()
    File("day07.txt").readLines().forEach { line ->
        val res = lineRegex.find(line)
        steps += res!!.groupValues[1][0]
        steps += res!!.groupValues[2][0]
        dependsOn.compute(res!!.groupValues[2][0]) { _, v -> (v ?: "") + res.groupValues[1][0] }
    }
    val workerCount = 5
    val workers = MutableList(workerCount) { Pair('-', 0) }
    while (steps.isNotEmpty()) {
        if (workers.any { it.first != '-' }) {
            val nextFreeWorker = workers.filter { it.first != '-' }.minBy { it.second }
            val nextWorker = Pair('-', nextFreeWorker!!.second)
            workers.remove(nextFreeWorker)
            workers.add(nextWorker)
            val lateWorkers = workers.filter { it.second < nextFreeWorker.second }
            workers.removeAll(lateWorkers)
            while (workers.size < workerCount)
                workers.add(nextWorker.copy())
            steps.remove(nextFreeWorker.first)
            dependsOn.remove(nextFreeWorker.first)
            dependsOn.forEach { k, v -> dependsOn[k] = v.replace(nextFreeWorker.first.toString(), "") }
        }
        while (workers.any { it.first == '-' }) {
            val nextSteps = TreeSet<Char>()
            nextSteps.addAll(dependsOn.filter { it.value.isEmpty() }.keys.sorted())
            nextSteps.addAll(steps.filter { !dependsOn.contains(it) })
            workers.forEach { nextSteps.remove(it.first) }
            if (nextSteps.isEmpty()) break
            val step = nextSteps.first()
            val freeWorker = workers.first { it.first == '-' }
            val newWorker = Pair(step, freeWorker.second + 60 + step.toInt() - 'A'.toInt() + 1)
            workers.remove(freeWorker)
            workers.add(newWorker)
        }
    }
    return workers.maxBy { it.second }!!.second
}

