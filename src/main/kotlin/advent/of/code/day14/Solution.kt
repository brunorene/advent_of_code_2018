package advent.of.code.day14

fun processUntil10(e1: Byte, e2: Byte, created: Int): List<Byte> {
    val recipes = mutableListOf(e1, e2)
    var elf1 = 0
    var elf2 = 1
    while (recipes.size < created + 10) {
        val combine = recipes[elf1] + recipes[elf2]
        val recipe1 = if (combine > 9) 1.toByte() else null
        val recipe2 = (combine % 10.toByte()).toByte()
        if (recipe1 != null) recipes += recipe1
        recipes += recipe2
        elf1 = (elf1 + recipes[elf1].toInt() + 1) % recipes.size
        elf2 = (elf2 + recipes[elf2].toInt() + 1) % recipes.size
    }
    return recipes
}

fun findSize(e1: Byte, e2: Byte, search: List<Byte>): Int {
    val recipes = mutableListOf(e1, e2)
    var elf1 = 0
    var elf2 = 1
    while (true) {
        val combine = recipes[elf1] + recipes[elf2]
        val recipe1 = if (combine > 9) 1.toByte() else null
        val recipe2 = (combine % 10.toByte()).toByte()
        if (recipe1 != null) {
            recipes += recipe1
            if (recipes.takeLast(search.size) == search)
                return recipes.size - search.size
        }
        recipes += recipe2
        if (recipes.takeLast(search.size) == search)
            return recipes.size - search.size
        elf1 = (elf1 + recipes[elf1].toInt() + 1) % recipes.size
        elf2 = (elf2 + recipes[elf2].toInt() + 1) % recipes.size
    }
}

fun part1() = processUntil10(3, 7, 920831).takeLast(10).joinToString("")

fun part2() = findSize(3, 7, listOf(9, 2, 0, 8, 3, 1))