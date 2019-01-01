package advent.of.code.day15

import java.io.File
import java.util.*
import kotlin.collections.LinkedHashSet
import kotlin.math.abs

data class Position(val x: Int, val y: Int) : Comparable<Position> {
    override fun compareTo(other: Position): Int {
        return compareBy<Position>({ it.y }, { it.x }).compare(this, other)
    }

    override fun toString(): String {
        return "(${x.toString().padStart(3, ' ')},${y.toString().padStart(3, ' ')})"
    }

    private fun up() = copy(y = y - 1)
    private fun down() = copy(y = y + 1)
    private fun left() = copy(x = x - 1)
    private fun right() = copy(x = x + 1)

    fun around(near: Position? = null) = listOf(up(), left(), right(), down())
        .filter { it.x >= 0 && it.y >= 0 }
        .sortedWith(compareBy({ it.distance(near ?: it) }, { it.y }, { it.x }))

    fun distance(b: Position) = abs(x - b.x) + abs(y - b.y)
}

fun Piece?.isDead() = (this?.hitPoints ?: 0) <= 0

sealed class Piece(open var position: Position, open val symbol: Char, open var hitPoints: Int = 200) :
    Comparable<Piece> {

    val y: Int
        get() = position.y

    val x: Int
        get() = position.x

    override fun compareTo(other: Piece) = position.compareTo(other.position)
}

abstract class Creature(
    override var position: Position,
    open val attack: Int = 3,
    override val symbol: Char
) : Piece(position, symbol) {

    fun damage(hit: Int) {
        hitPoints -= hit
    }

    abstract fun enemy(crit: Creature?): Boolean
}

const val WALL = '#'
const val ELF = 'E'
const val GOBLIN = 'G'

data class Elf(override var position: Position, override val attack: Int = 3, override var hitPoints: Int = 200) :
    Creature(position, attack, ELF) {
    override fun enemy(crit: Creature?) = crit is Goblin
}

data class Goblin(override var position: Position, override val attack: Int = 3, override var hitPoints: Int = 200) :
    Creature(position, attack, GOBLIN) {
    override fun enemy(crit: Creature?) = crit is Elf
}

data class Wall(override var position: Position) : Piece(position, WALL)

class GameBoard(initFile: File) {
    var pieces: MutableSet<Piece> = TreeSet()

    val elves: Set<Elf>
        get() = pieces.mapNotNullTo(LinkedHashSet()) { (it as? Elf) }

    val goblins: Set<Goblin>
        get() = pieces.mapNotNullTo(LinkedHashSet()) { (it as? Goblin) }

    val creatures: Set<Creature>
        get() = pieces.mapNotNullTo(LinkedHashSet()) { (it as? Creature) }

    init {
        initFile.readLines().forEachIndexed { y, line ->
            line.forEachIndexed { x, symbol ->
                val pos = Position(x, y)
                when (symbol) {
                    WALL -> Wall(pos)
                    ELF -> Elf(pos)
                    GOBLIN -> Goblin(pos)
                    else -> null
                }?.let {
                    pieces.add(it)
                }
            }
        }
    }

    fun inRange(crit: Creature): Pair<Creature, Set<Position>> = crit to pieces.mapNotNull { it as? Creature }
        .filter { crit.enemy(it) }
        .flatMap { it.position.around() }
        .filterTo(TreeSet()) { (piece(it) !is Creature) and (piece(it) !is Wall) }

    private fun distance(
        pos1: Position,
        pos2: Position,
        pastPos: MutableList<Position> = mutableListOf(),
        distance: Int = 0
    ): Int? =
        when {
            piece(pos1) is Wall -> null
            (piece(pos1) is Creature) and !piece(pos1).isDead() -> null
            pos1 in pastPos -> null
            pos1 == pos2 -> distance
            else -> pos1.around(pos2)
                .mapNotNull { distance(it, pos2, pastPos.apply { this += pos1 }, distance + 1) }.min()
        }

    fun nearestReachable(data: Pair<Creature, Set<Position>>) =
        data.second.flatMap {
            data.first.position.around(it)
                .map { a -> a to distance(a, it) }
                .filter { p -> p.second != null }
        }.minWith(compareBy({ it.second ?: Int.MAX_VALUE }, { it.first.y }, { it.first.x }))
            ?.first

    fun buryDead() {
        pieces = pieces.filterTo(TreeSet()) { it.hitPoints > 0 }
    }

    fun piece(pos: Position?) = pieces.firstOrNull { it.position == pos }

    override fun toString(): String {
        var output = ""
        for (y in (0..(pieces.map { it.y }.max() ?: 0))) {
            for (x in (0..(pieces.map { it.x }.max() ?: 0)))
                output += piece(Position(x, y))?.symbol ?: '.'
            output += '\n'
        }
        return output
    }
}

val input = File("day15-test.txt")

fun part1(): Int {
    val board = GameBoard(input)
    var round = 0
    println("init")
    println(board)
    while (board.elves.isNotEmpty() and board.goblins.isNotEmpty()) {
        round++
        for (crit in board.creatures) {
            if (crit.isDead())
                continue

            //move
            if (crit.position.around().none { crit.enemy(board.piece(it) as? Creature) }) {
                val chosenPos = board.nearestReachable(board.inRange(crit))
                if (chosenPos != null)
                    crit.position = chosenPos
            }

            val target = crit.position.around().filter { crit.enemy(board.piece(it) as? Creature) }
                .mapNotNull { board.piece(it) as? Creature }
                .sortedWith(compareBy({ it.hitPoints }, { it.y }, { it.x }))
                .firstOrNull()

            target?.damage(crit.attack)
        }
        board.buryDead()
        println("$round (${board.creatures.size}) -> ${round * board.creatures.map { it.hitPoints }.sum()}")
        println(board)
        println(board.creatures.joinToString("\n"))
        println()
    }

    return 0
}

fun part2() = 2