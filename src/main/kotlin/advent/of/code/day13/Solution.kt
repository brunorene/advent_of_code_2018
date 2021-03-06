package advent.of.code.day13

import advent.of.code.day13.Direction.*
import advent.of.code.day13.Forward.*
import java.io.File
import java.util.*

const val ANSI_RESET = "\u001B[0m"
const val ANSI_RED = "\u001B[31m"
//val ANSI_BLACK = "\u001B[30m"
//val ANSI_GREEN = "\u001B[32m"
//val ANSI_YELLOW = "\u001B[33m"
//val ANSI_BLUE = "\u001B[34m"
//val ANSI_PURPLE = "\u001B[35m"
//val ANSI_CYAN = "\u001B[36m"
//val ANSI_WHITE = "\u001B[37m"

const val FILENAME = "day13.txt"

enum class Direction { UP, DOWN, LEFT, RIGHT }

enum class Forward { STRAIGHT, TURN_LEFT, TURN_RIGHT }

data class Position(var x: Int, var y: Int) : Comparable<Position> {
    var removed: Boolean = false

    override fun compareTo(other: Position): Int {
        return compareBy<Position>({ it.y }, { it.x }).compare(this, other)
    }

    override fun toString(): String {
        return "(${x.toString().padStart(3, '0')},${y.toString().padStart(3, '0')})"
    }

    fun move(direction: Direction?) = when (direction) {
        UP -> copy(y = y - 1)
        DOWN -> copy(y = y + 1)
        LEFT -> copy(x = x - 1)
        RIGHT -> copy(x = x + 1)
        else -> copy()
    }
}

data class Cart(
    var direction: Direction,
    val nextDirection: Deque<Forward> = ArrayDeque(listOf(TURN_LEFT, STRAIGHT, TURN_RIGHT))
) {

    private fun intersectionForward(): Forward {
        val f = nextDirection.pop()
        nextDirection.addLast(f)
        return f
    }

    fun changeDirection(track: Track?): Cart {
        val newDirection = if (track is Intersection) {
            val forward = intersectionForward()
            when (forward) {
                STRAIGHT -> direction
                TURN_LEFT -> when (direction) {
                    UP -> LEFT
                    DOWN -> RIGHT
                    LEFT -> DOWN
                    RIGHT -> UP
                }
                TURN_RIGHT -> when (direction) {
                    UP -> RIGHT
                    DOWN -> LEFT
                    LEFT -> UP
                    RIGHT -> DOWN
                }

            }
        } else {
            when (track) {
                is ForwardSlashCurve -> when (direction) {
                    UP -> RIGHT
                    DOWN -> LEFT
                    LEFT -> DOWN
                    RIGHT -> UP
                }
                is BackSlashCurve -> when (direction) {
                    UP -> LEFT
                    DOWN -> RIGHT
                    LEFT -> UP
                    RIGHT -> DOWN
                }
                else -> direction
            }
        }
        return copy(direction = newDirection)
    }
}

sealed class Track(open val position: Position, open var cart: Cart?)
data class VerticalLine(override val position: Position, override var cart: Cart? = null) : Track(position, cart)
data class HorizontalLine(override val position: Position, override var cart: Cart? = null) : Track(position, cart)
data class ForwardSlashCurve(override val position: Position, override var cart: Cart? = null) :
    Track(position, cart)

data class BackSlashCurve(override val position: Position, override var cart: Cart? = null) : Track(position, cart)
data class Intersection(override val position: Position, override var cart: Cart? = null) : Track(position, cart)

class TrackMap(lines: List<String>) {
    private val tracks: MutableMap<Position, Track?> = mutableMapOf()
    var cartPositions: MutableList<Position> = mutableListOf()

    init {
        lines.forEachIndexed { y, line ->
            line.forEachIndexed { x, track ->
                val p = Position(x, y)
                tracks[p] = when (track) {
                    '/' -> ForwardSlashCurve(p)
                    '\\' -> BackSlashCurve(p)
                    '-' -> HorizontalLine(p)
                    '|' -> VerticalLine(p)
                    '+' -> Intersection(p)
                    '<' -> processCart(LEFT, p)
                    '>' -> processCart(RIGHT, p)
                    'v' -> processCart(DOWN, p)
                    '^' -> processCart(UP, p)
                    else -> null
                }
            }
        }
    }

    override fun toString() =
        tracks.keys.sortedWith(compareBy({ it.y }, { it.x }))
            .joinToString("") { p ->
                val path = track(p)
                val pixel = if (p.x == 0 && p.y > 0) "\n" else ""
                pixel + if (path is Track) {
                    val cart = path.cart
                    if (cart is Cart)
                        ANSI_RED + when (cart.direction) {
                            UP -> '^'
                            DOWN -> 'v'
                            LEFT -> '<'
                            RIGHT -> '>'
                        } + ANSI_RESET
                    else when (path) {
                        is HorizontalLine -> '-'
                        is VerticalLine -> '|'
                        is ForwardSlashCurve -> '/'
                        is BackSlashCurve -> '\\'
                        is Intersection -> '+'
                    }
                } else " "
            }

    private fun track(p: Position) = if (p.x >= 0 && p.y >= 0) tracks[p] else null

    private fun processCart(direction: Direction, p: Position): Track {
        val cart = Cart(direction)
        cartPositions.add(p)
        return if (direction in listOf(UP, DOWN)) VerticalLine(p, cart) else HorizontalLine(p, cart)
    }

    fun moveCarts(print: Boolean) {
        if (print) {
            print("\u001b[H\u001b[2J")
            println(toString())
            Thread.sleep(3000)
        }
        val currentPos = cartPositions.sorted().toMutableList()
        for (idx in (0 until currentPos.size)) {
            val currentTrack = track(currentPos[idx])
            var newCart = currentTrack?.cart
            val newPosition = currentPos[idx].move(newCart?.direction)
            if (currentPos.contains(newPosition))
                throw CollisionException(newPosition)
            val newTrack = track(newPosition)
            newCart = newCart?.changeDirection(newTrack)
            currentTrack?.cart = null
            newTrack?.cart = newCart
            currentPos[idx] = newPosition
        }
        cartPositions = currentPos.sorted().toMutableList()
        println(cartPositions)
    }

    fun moveCartsAndRemove(print: Boolean): Position? {
        if (print) {
            print("\u001b[H\u001b[2J")
            println(toString())
            Thread.sleep(3000)
        }
        var currentPos = cartPositions.sorted().toMutableList()
        var idx = 0
        while (idx < currentPos.size) {
            if (currentPos[idx].removed) {
                idx++
                continue
            }
            val currentTrack = track(currentPos[idx])
            var newCart = currentTrack?.cart
            val newPosition = currentPos[idx].move(newCart?.direction)
            val newTrack = track(newPosition)
            newCart = newCart?.changeDirection(newTrack)
            currentTrack?.cart = null
            newTrack?.cart = newCart
            currentPos[idx] = newPosition
            if (currentPos.filter { it == newPosition && !it.removed }.size > 1)
                currentPos.forEach { it.removed = (it == newPosition || it.removed) }
            idx++
        }
        cartPositions = currentPos.sorted().toMutableList()
        if (cartPositions.filter { !it.removed }.count() == 1) {
            val single = cartPositions.first { !it.removed }
            val t = track(single)
            return single.move(t?.cart?.direction)
        }
        println(cartPositions.count { it.removed })
        println(cartPositions.map { "$it${if (it.removed) "X" else ""}" })
        return null
    }
}

class CollisionException(val collisionPosition: Position) : Exception()

fun part1(): String {
    val map = TrackMap(File(FILENAME).readLines())
    return try {
        while (true) {
            if (map.moveCartsAndRemove(false) != null)
                break
        }
        map.cartPositions.first { !it.removed }.toString()
    } catch (ex: CollisionException) {
        "${ex.collisionPosition.x},${ex.collisionPosition.y}"
    }
}