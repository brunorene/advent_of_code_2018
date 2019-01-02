package advent.of.code.day16

import java.io.File

val input = File("day16.txt")

class CPU {
    val commands: List<CPU.(Int, Int, Int) -> Unit> =
        listOf(
            CPU::addr, CPU::addi, CPU::mulr, CPU::muli, CPU::banr, CPU::bani, CPU::borr, CPU::bori,
            CPU::setr, CPU::seti, CPU::gtir, CPU::gtri, CPU::gtrr, CPU::eqir, CPU::eqri, CPU::eqrr
        )

    var reg: MutableList<Int> = mutableListOf(-1, -1, -1, -1)

    fun addr(a: Int, b: Int, c: Int) {
        reg[c] = reg[a] + reg[b]
    }

    fun addi(a: Int, b: Int, c: Int) {
        reg[c] = reg[a] + b
    }

    fun mulr(a: Int, b: Int, c: Int) {
        reg[c] = reg[a] * reg[b]
    }

    fun muli(a: Int, b: Int, c: Int) {
        reg[c] = reg[a] * b
    }

    fun banr(a: Int, b: Int, c: Int) {
        reg[c] = reg[a] and reg[b]
    }

    fun bani(a: Int, b: Int, c: Int) {
        reg[c] = reg[a] and b
    }

    fun borr(a: Int, b: Int, c: Int) {
        reg[c] = reg[a] or reg[b]
    }

    fun bori(a: Int, b: Int, c: Int) {
        reg[c] = reg[a] or b
    }

    @Suppress("UNUSED_PARAMETER")
    fun setr(a: Int, b: Int, c: Int) {
        reg[c] = reg[a]
    }

    @Suppress("UNUSED_PARAMETER")
    fun seti(a: Int, b: Int, c: Int) {
        reg[c] = a
    }

    fun gtir(a: Int, b: Int, c: Int) {
        reg[c] = if (a > reg[b]) 1 else 0
    }

    fun gtri(a: Int, b: Int, c: Int) {
        reg[c] = if (reg[a] > b) 1 else 0
    }

    fun gtrr(a: Int, b: Int, c: Int) {
        reg[c] = if (reg[a] > reg[b]) 1 else 0
    }

    fun eqir(a: Int, b: Int, c: Int) {
        reg[c] = if (a == reg[b]) 1 else 0
    }

    fun eqri(a: Int, b: Int, c: Int) {
        reg[c] = if (reg[a] == b) 1 else 0
    }

    fun eqrr(a: Int, b: Int, c: Int) {
        reg[c] = if (reg[a] == reg[b]) 1 else 0
    }
}

fun part1(): Int {
    val cpu = CPU()
    val beforeRegex = Regex("Before: \\[([0-9]+), ([0-9]+), ([0-9]+), ([0-9]+)]")
    val afterRegex = Regex("After:  \\[([0-9]+), ([0-9]+), ([0-9]+), ([0-9]+)]")
    val commandRegex = Regex("^([0-9]+) ([0-9]+) ([0-9]+) ([0-9]+)")
    input.bufferedReader().use { reader ->
        var sampleCount = 0
        while (true) {
            var line = reader.readLine().trim()
            val beforeResult = beforeRegex.find(line)
            line = reader.readLine().trim()
            val commandResult = commandRegex.find(line)
            line = reader.readLine().trim()
            val afterResult = afterRegex.find(line)
            reader.readLine().trim()
            if (beforeResult == null)
                break
            val before = beforeResult.groupValues.drop(1).map { it.toInt() }
            val command = commandResult?.groupValues?.drop(2)?.map { it.toInt() }!!
            val after = afterResult?.groupValues?.drop(1)?.map { it.toInt() }!!
            var count = 0
            cpu.commands.forEach { comm ->
                cpu.reg = mutableListOf(before[0], before[1], before[2], before[3])
                comm(cpu, command[0], command[1], command[2])
                if (cpu.reg == mutableListOf(after[0], after[1], after[2], after[3]))
                    count++
            }
            if (count >= 3)
                sampleCount++
        }
        return sampleCount
    }
}

fun part2(): Int {
    val cpu = CPU()
    val beforeRegex = Regex("Before: \\[([0-9]+), ([0-9]+), ([0-9]+), ([0-9]+)]")
    val afterRegex = Regex("After:  \\[([0-9]+), ([0-9]+), ([0-9]+), ([0-9]+)]")
    val commandRegex = Regex("^([0-9]+) ([0-9]+) ([0-9]+) ([0-9]+)")
    input.bufferedReader().use { reader ->
        val commandMap = mutableMapOf<Int, MutableSet<CPU.(Int, Int, Int) -> Unit>>()
        while (true) {
            var line = reader.readLine().trim()
            val beforeResult = beforeRegex.find(line) ?: break
            line = reader.readLine().trim()
            val commandResult = commandRegex.find(line)
            line = reader.readLine().trim()
            val afterResult = afterRegex.find(line)
            reader.readLine().trim()
            val before = beforeResult.groupValues.drop(1).map { it.toInt() }
            val command = commandResult?.groupValues?.drop(1)?.map { it.toInt() }!!
            val after = afterResult?.groupValues?.drop(1)?.map { it.toInt() }!!
            cpu.commands.forEach { comm ->
                cpu.reg = mutableListOf(before[0], before[1], before[2], before[3])
                comm(cpu, command[1], command[2], command[3])
                if (cpu.reg == mutableListOf(after[0], after[1], after[2], after[3])) {
                    val nameSet = commandMap.computeIfAbsent(command[0]) { mutableSetOf() }
                    nameSet += comm
                }
            }
        }
        val singleCommandMap = mutableMapOf<Int, CPU.(Int, Int, Int) -> Unit>()
        while (commandMap.isNotEmpty()) {
            commandMap.filter { it.value.size == 1 }.forEach {
                singleCommandMap[it.key] = it.value.first()
            }
            commandMap.values.forEach { c -> singleCommandMap.values.forEach { sc -> c.remove(sc) } }
            val keys = commandMap.filter { it.value.isEmpty() }.keys
            keys.forEach { commandMap.remove(it) }
        }
        singleCommandMap.forEach { commId, comm ->
            println("$commId $comm")
        }
        reader.readLine().trim()
        cpu.reg = mutableListOf(0, 0, 0, 0)
        reader.lineSequence().forEach { line ->
            val command = commandRegex.find(line)?.groupValues?.drop(1)?.map { it.toInt() }!!
            singleCommandMap[command[0]]?.invoke(cpu, command[1], command[2], command[3])
        }
        println(cpu.reg[0])
    }
    return 1
}