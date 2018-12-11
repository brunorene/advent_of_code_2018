package advent.of.code.day11

const val id = 5535

val squareResults = mutableMapOf<Pair<Int, Int>, Pair<Int, Int>>()

fun powerLevel(x: Int, y: Int) = (((((x + 10) * y + id) * (x + 10)) / 100) % 10) - 5

fun part1(): String {
    val result = (1..298).map { x ->
        (1..298).map { y ->
            val power = (0..2).map { powerX ->
                (0..2).map { powerY ->
                    powerLevel(x + powerX, y + powerY)
                }.sum()
            }.sum()
            Triple(x, y, power)
        }.maxBy { it.third }
    }.maxBy { it?.third ?: 0 }
    return "${result?.first},${result?.second}"
}

fun part2(): String {
    var max = 0
    var maxRegion: Triple<Int, Int, Int>? = null
    for (size in (1..300)) {
        for (x in (1..(301 - size))) {
            for (y in (1..(301 - size))) {
                val power = if (size > 1) {
                    squareResults[Pair(x, y)]?.first!! +
                            (x..(x + size - 2)).map {
                                powerLevel(it, y + size - 1)
                            }.sum() +
                            (y..(y + size - 2)).map {
                                powerLevel(x + size - 1, it)
                            }.sum() +
                            powerLevel(x + size - 1, y + size - 1)
                } else {
                    (x..(x + size - 1)).map { innerX ->
                        (y..(y + size - 1)).map { innerY ->
                            powerLevel(innerX, innerY)
                        }.sum()
                    }.sum()
                }
                if (power > max) {
                    max = power
                    maxRegion = Triple(x, y, size)
                }
                squareResults[Pair(x, y)] = Pair(power, size)
            }
        }

    }
    return "${maxRegion?.first},${maxRegion?.second},${maxRegion?.third}"
}

