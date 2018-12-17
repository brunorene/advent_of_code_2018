package advent.of.code.day15

import java.io.File
import java.util.*
import kotlin.math.abs

data class Position(val x: Int, val y: Int) : Comparable<Position> {
    override fun compareTo(other: Position): Int {
        return compareBy<Position>({ it.y }, { it.x }).compare(this, other)
    }

    override fun toString(): String {
        return "(${x.toString().padStart(3, ' ')},${y.toString().padStart(3, ' ')})"
    }

    fun up() = copy(y = y - 1)
    fun down() = copy(y = y + 1)
    fun left() = copy(x = x - 1)
    fun right() = copy(x = x + 1)

    fun around() = listOf(up(), down(), left(), right())

    fun distance(b: Position) = abs(x - b.x) + abs(y - b.y)
}

sealed class Piece(open val position: Position, open val symbol: Char) : Comparable<Piece> {

    override fun compareTo(other: Piece) = position.compareTo(other.position)
}

abstract class Creature(
    override val position: Position,
    open var hitPoints: Int = 200,
    open val attack: Byte = 3,
    override val symbol: Char
) : Piece(position, symbol) {

    fun damage(hit: Int): Int {
        hitPoints -= hit
        return hitPoints
    }

    abstract fun duplicate(newPos: Position = position): Creature
}

const val WALL = '#'
const val ELF = 'E'
const val GOBLIN = 'G'

data class Elf(override val position: Position, override var hitPoints: Int = 200, override val attack: Byte = 3) :
    Creature(position, hitPoints, attack, ELF) {

    override fun duplicate(newPos: Position) = copy(position = newPos)
}

data class Goblin(override val position: Position, override var hitPoints: Int = 200, override val attack: Byte = 3) :
    Creature(position, hitPoints, attack, GOBLIN) {

    override fun duplicate(newPos: Position) = copy(position = newPos)
}

data class Wall(override val position: Position) : Piece(position, WALL)

class GameBoard(initFile: File) {
    var pieces: MutableSet<Piece> = TreeSet()
    var mapPieces: MutableMap<Position, Piece> = mutableMapOf()

    init {
        initFile.readLines().forEachIndexed { y, line ->
            line.forEachIndexed { x, symbol ->
                val pos = Position(x, y)
                when (symbol) {
                    WALL -> Wall(pos)
                    ELF -> Elf(pos)
                    GOBLIN -> Goblin(pos)
                    else -> null
                }?.apply {
                    pieces.add(this)
                    mapPieces[pos] = this
                }
            }
        }
    }

    fun removePiece(piece: Piece) {
        pieces.remove(piece)
        mapPieces.remove(piece.position)
    }

    fun addPiece(piece: Piece) {
        pieces.add(piece)
        mapPieces.put(piece.position, piece)
    }

    fun piece(pos: Position) = mapPieces[pos]

    val elfs: Set<Elf>
        get() = pieces.filter { it is Elf }
            .map { it as Elf }
            .map { it.duplicate() }
            .toCollection(TreeSet())

    val goblins: Set<Goblin>
        get() = pieces.filter { it is Goblin }
            .map { it as Goblin }
            .map { it.duplicate() }
            .toCollection(TreeSet())

    val walls: Set<Wall>
        get() = pieces.filter { it is Wall }
            .map { it as Wall }
            .map { it.copy() }
            .toCollection(TreeSet())

    val creatures: Set<Creature>
        get() = pieces.filter { it is Creature }
            .map { it as Creature }
            .map { it.duplicate() }
            .toCollection(TreeSet())

    private fun minPath(start: Position, end: Position, past: MutableSet<Position>): List<Position>? = when {
        piece(start) is Wall -> null
        piece(start) is Creature -> null
        past.contains(start) -> null
        start == end -> listOf(end)
        else -> {
            start.around()
                .sortedBy { it.distance(end) }
                .mapNotNull { minPath(it, end, past.apply { add(start) }) }
                .firstOrNull()
                ?.toMutableList()
                ?.apply { add(0, start) }
        }
    }

    fun shortestPath(ally: Creature, aroundEnemyPosition: Position) = ally.position.around()
        .filter { piece(it) == null }
        .sortedBy { it.distance(aroundEnemyPosition) }
        .mapNotNull { near1 -> minPath(near1, aroundEnemyPosition, mutableSetOf()) }
        .minBy { it.size }

    override fun toString(): String {
        var output = ""
        for (y in (0..(pieces.map { it.position.y }.max() ?: 0))) {
            for (x in (0..(pieces.map { it.position.x }.max() ?: 0)))
                output += piece(Position(x, y))?.symbol ?: '.'
            output += '\n'
        }
        return output
    }
}

val input = File("day15.txt")

fun part1(): Int {
    val board = GameBoard(input)
    val movedCrits = mutableListOf<Creature>()
    for (crit in board.creatures) {
        board.removePiece(crit)
        val enemies = when (crit) {
            is Elf -> board.goblins
            is Goblin -> board.elfs
            else -> emptySet()
        }
        val candidates = mutableListOf<List<Position>>()
        for (enemy in enemies) {
            val posAround = enemy.position.around()
                .filter { board.piece(it) == null }
                .sortedBy { crit.position.distance(it) }
            board.shortestPath(crit, posAround[0]) ?: break
            for (near in posAround) {
                candidates += board.shortestPath(crit, near) ?: break
            }
        }
        if (candidates.isEmpty())
            board.addPiece(crit)
        else
            movedCrits += crit.duplicate(candidates.sortedBy { it.size }.first()[0])
    }

    return 0
}

fun part2() = 2