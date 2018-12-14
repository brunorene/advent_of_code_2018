package advent.of.code.day12

import java.io.File

// STILL WRONG :(

data class PlantsInPots(val data: MutableSet<Long> = mutableSetOf()) {

    companion object {
        fun build(init: String): PlantsInPots {
            val instance = PlantsInPots()
            init.forEachIndexed { index, c -> instance[index.toLong()] = c }
            return instance
        }
    }

    fun copy() = PlantsInPots(data.toMutableSet())

    operator fun get(index: Long) = if (data.contains(index)) '#' else '.'

    operator fun set(index: Long, c: Char) = if (c == '#') data.add(index) else data.remove(index)

    fun sumPlantIndexes() = data.sum()

    fun plantCount() = data.size

    fun min() = data.min() ?: 0

    fun max() = data.max() ?: 0

    fun slice(middle: Long, borderSize: Long) =
        ((middle - borderSize)..(middle + borderSize)).map { get(it) }.joinToString("")

    override fun toString() =
        (-10..max()).joinToString("") { idx ->
            if (idx == 0L) get(idx).toString()
                .replace('#', 'X')
                .replace('.', 'o')
            else get(idx).toString()
        }
}

const val initState =
    "#...##.#...#..#.#####.##.#..###.#.#.###....#...#...####.#....##..##..#..#..#..#.#..##.####.#.#.###"

val rules = File("day12.txt").readLines().drop(2)
    .map { it.substring((0..4)) to it[9] }

fun calculate(lasIteration: Int): Long {
    val generation = PlantsInPots.build(initState)
//    println(info(0, generation))
    for (g in (1..lasIteration)) {
        val before = generation.copy()
        ((before.min() - 5)..(before.max() + 5))
            .map { pos ->
                val pattern = before.slice(pos, 2)
                val rule = rules.firstOrNull { pattern == it.first }
                generation[pos] = rule?.second ?: '.'
            }
//        println(info(g, generation))
    }
    return generation.sumPlantIndexes()
}

fun part1() = calculate(20)

fun part2(): Long {
    val res200 = calculate(200)
    val res300 = calculate(300)
//    val res400 = calculate(400)
    val diff = res300 - res200
//    println("${res400 - res300} ${res300 - res200}")
    return (50_000_000_000 - 200) / 100 * diff + res200
}

fun info(g: Int, gen: PlantsInPots) =
    g.toString().padStart(4, '0') +
            " - $gen" +
            " - ${gen.sumPlantIndexes()}" +
            " - ${gen.plantCount()}" +
            " - ${gen.min()}" +
            " - ${gen.max()}"