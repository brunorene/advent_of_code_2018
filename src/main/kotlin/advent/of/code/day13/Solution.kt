package advent.of.code.day13

import advent.of.code.day13.Direction.*
import advent.of.code.day13.Forward.*
import java.io.File
import java.util.*

val ANSI_RESET = "\u001B[0m"
val ANSI_RED = "\u001B[31m"
//val ANSI_BLACK = "\u001B[30m"
//val ANSI_GREEN = "\u001B[32m"
//val ANSI_YELLOW = "\u001B[33m"
//val ANSI_BLUE = "\u001B[34m"
//val ANSI_PURPLE = "\u001B[35m"
//val ANSI_CYAN = "\u001B[36m"
//val ANSI_WHITE = "\u001B[37m"

const val FILENAME = "day13.txt"

data class Point(val x: Int, val y: Int) {
    override fun toString(): String {
        return "(${x.toString().padStart(3, '0')},${y.toString().padStart(3, '0')})"
    }
}

enum class Direction { UP, DOWN, LEFT, RIGHT }

enum class Forward { STRAIGHT, TURN_LEFT, TURN_RIGHT }

sealed class Vehicle
data class Cart(var direction: Direction) : Vehicle() {
    private val nextDirection = ArrayDeque(listOf(TURN_LEFT, STRAIGHT, TURN_RIGHT))

    private fun intersectionForward(): Forward {
        val f = nextDirection.pop()
        nextDirection.addLast(f)
        return f
    }

    fun moveForward(p: Point, map: TrackMap): Point? {
        val nextPoint = when (direction) {
            UP -> p.copy(y = p.y - 1)
            DOWN -> p.copy(y = p.y + 1)
            LEFT -> p.copy(x = p.x - 1)
            RIGHT -> p.copy(x = p.x + 1)
        }
        val nextPath = map.track(nextPoint)
        direction = when (nextPath) {
            is UpRightCurve -> when (direction) {
                UP -> direction
                DOWN -> RIGHT
                LEFT -> UP
                RIGHT -> direction
            }
            is UpLeftCurve -> when (direction) {
                UP -> direction
                DOWN -> LEFT
                LEFT -> direction
                RIGHT -> UP
            }
            is DownLeftCurve -> when (direction) {
                UP -> LEFT
                DOWN -> direction
                LEFT -> direction
                RIGHT -> DOWN
            }
            is DownRightCurve -> when (direction) {
                UP -> RIGHT
                DOWN -> direction
                LEFT -> DOWN
                RIGHT -> direction
            }
            is Intersection -> when (intersectionForward()) {
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
            else -> direction
        }
        return nextPoint
    }
}

object NoCart : Vehicle()

sealed class Path()
abstract class Track(open var cart: Vehicle) : Path()
data class VerticalLine(override var cart: Vehicle = NoCart) : Track(cart)
data class HorizontalLine(override var cart: Vehicle = NoCart) : Track(cart)
data class UpRightCurve(override var cart: Vehicle = NoCart) : Track(cart)
data class UpLeftCurve(override var cart: Vehicle = NoCart) : Track(cart)
data class DownLeftCurve(override var cart: Vehicle = NoCart) : Track(cart)
data class DownRightCurve(override var cart: Vehicle = NoCart) : Track(cart)
data class Intersection(override var cart: Vehicle = NoCart) : Track(cart)
object NoTrack : Path()

class TrackMap(lines: List<String>) {
    private val tracks: MutableMap<Point, Path> = mutableMapOf()
    private var cartPositions: MutableList<Point> = mutableListOf()

    init {
        val carts = mutableListOf<Pair<Point, Char>>()
        lines.forEachIndexed { y, line ->
            line.forEachIndexed { x, track ->
                val p = Point(x, y)
                tracks[p] = when (track) {
                    '/' -> if (left(p) is HorizontalLine || left(p) is Intersection) UpLeftCurve() else DownRightCurve()
                    '\\' -> if (left(p) is HorizontalLine || left(p) is Intersection) DownLeftCurve() else UpRightCurve()
                    '-' -> HorizontalLine()
                    '|' -> VerticalLine()
                    '+' -> Intersection()
                    else -> {
                        if (track in listOf('<', '>', 'v', '^')) carts += Pair(p, track)
                        NoTrack
                    }
                }
            }
        }
        carts.forEach { (pos, track) ->
            tracks[pos] = when (track) {
                '<' -> processCart(LEFT, pos)
                '>' -> processCart(RIGHT, pos)
                'v' -> processCart(DOWN, pos)
                '^' -> processCart(UP, pos)
                else -> NoTrack
            }
        }
    }

    override fun toString() =
        tracks.keys.sortedWith(compareBy({ it.y }, { it.x }))
            .joinToString("") { p ->
                val path = track(p)
                val pixel = if (p.x == 0 && p.y > 0) "\n" else ""
                pixel + if (cartPositions.contains(p) && path is Track) {
                    val cart = path.cart
                    if (cart is Cart)
                        ANSI_RED + when (cart.direction) {
                            UP -> "^"
                            DOWN -> "v"
                            LEFT -> "<"
                            RIGHT -> ">"
                        } + ANSI_RESET
                    else '$'
                } else
                    when (path) {
                        is HorizontalLine -> '-'
                        is VerticalLine -> '|'
                        is UpLeftCurve -> '/'
                        is DownRightCurve -> '/'
                        is DownLeftCurve -> '\\'
                        is UpRightCurve -> '\\'
                        is Intersection -> '+'
                        else -> ' '
                    }
            }

    fun track(p: Point) = if (p.x < 0 || p.y < 0) NoTrack else tracks[p] ?: NoTrack

    private fun left(p: Point) = track(p.copy(x = p.x - 1))

    private fun processCart(direction: Direction, p: Point): Path {
        cartPositions.add(p)
        cartPositions.sortWith(compareBy({ it.y }, { it.x }))
        return if (direction in listOf(UP, DOWN)) VerticalLine(Cart(direction))
        else HorizontalLine(Cart(direction))
    }

    fun moveCarts(print: Boolean) {
        if (print) {
            print("\u001b[H\u001b[2J")
            println(toString())
        }
        cartPositions = cartPositions.map { cartPos ->
            val path = track(cartPos)
            var newPos = cartPos
            if (path is Track) {
                val cart = path.cart
                if (cart is Cart) {
                    val next = cart.moveForward(cartPos, this)
                    if (next != null) {
                        if (cartPositions.contains(next))
                            throw CollisionException(next)
                        newPos = next
                        path.cart = NoCart
                        (track(newPos) as? Track)?.cart = cart
                    }
                }
            }
            newPos
        }.sortedWith(compareBy({ it.y }, { it.x }))
            .toMutableList()
    }
}

class CollisionException(val collisionPoint: Point) : Exception()

fun part1(): String {
    val map = TrackMap(File(FILENAME).readLines())
    return try {
        (1..20000).forEach { Thread.sleep(5); map.moveCarts(false) }
        "no collisions"
    } catch (ex: CollisionException) {
        "${ex.collisionPoint.x},${ex.collisionPoint.y}"
    }
}

fun part2() = 2