package advent.of.code.day12

import java.io.File

// STILL WRONG :(

class PlantsInPots(init: String) {
    private val data = mutableSetOf<Long>()

    init {
        init.forEachIndexed { index, c -> set(index.toLong(), c) }
    }

    operator fun get(index: Long) = if (data.contains(index)) '#' else '.'

    operator fun set(index: Long, c: Char) = if (c == '#') data.add(index) else data.remove(index)

    fun sumPlantIndexes() = data.sum()

    fun plantCount() = data.size

    fun min() = data.min() ?: 0

    fun max() = data.max() ?: 0

    fun slice(middle: Long, borderSize: Long) =
        ((middle - borderSize)..(middle + borderSize)).map { get(it) }.joinToString("")

    override fun toString() =
        (min()..max()).mapIndexed { idx, v -> if (idx == 0 && get(v) == '#') 'X' else get(v) }.joinToString("")
}

const val initState =
    "#...##.#...#..#.#####.##.#..###.#.#.###....#...#...####.#....##..##..#..#..#..#.#..##.####.#.#.###"

val rules = File("day12.txt").readLines().drop(2)
    .map { it.substring((0..4)) to it[9] }

fun calculate(lasIteration: Int): Long {
    val generation = PlantsInPots(initState)
    for (g in (1..lasIteration)) {
        ((generation.min() - 5)..(generation.max() + 5))
            .map { pos ->
                val pattern = generation.slice(pos, 2)
                val rule = rules.firstOrNull { pattern == it.first }
                generation[pos] = rule?.second ?: '.'
            }
        println(info(g, generation))
    }
    return generation.sumPlantIndexes()
}

fun part1() = calculate(20)

fun part2(): Long {
//    val res200 = calculate(200)
//    val res300 = calculate(300)
//    val res400 = calculate(400)
//    println("${res400 - res300} ${res300 - res200}")
    return 0L
}

fun info(g: Int, gen: PlantsInPots) =
    g.toString().padStart(4, '0') +
            " - $gen" +
            " - ${gen.sumPlantIndexes()}" +
            " - ${gen.plantCount()}" +
            " - ${gen.min()}" +
            " - ${gen.max()}"