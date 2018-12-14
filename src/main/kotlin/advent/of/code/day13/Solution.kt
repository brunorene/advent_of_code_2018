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

data class Point(var x: Int, var y: Int) {
    override fun toString(): String {
        return "(${x.toString().padStart(3, '0')},${y.toString().padStart(3, '0')})"
    }

    fun move(direction: Direction) {
        when (direction) {
            UP -> y--
            DOWN -> y++
            LEFT -> x--
            RIGHT -> x++
        }

    }
}

enum class Direction { UP, DOWN, LEFT, RIGHT }

enum class Forward { STRAIGHT, TURN_LEFT, TURN_RIGHT }

sealed class Vehicle
data class Cart(var direction: Direction, var position: Point) : Vehicle(), Comparable<Cart> {

    private val comparator: Comparator<Cart> = compareBy({ it.position.y }, { it.position.x })

    override fun compareTo(other: Cart) = comparator.compare(this, other)

    private val nextDirection = ArrayDeque(listOf(TURN_LEFT, STRAIGHT, TURN_RIGHT))

    private fun intersectionForward(): Forward {
        val f = nextDirection.pop()
        nextDirection.addLast(f)
        return f
    }

    fun moveForward(map: TrackMap) {
        position.move(direction)
        val nextPath = map.track(position)
        direction = when (nextPath) {
            is ForwardSlashCurve -> when (direction) {
                UP -> RIGHT
                DOWN -> LEFT
                LEFT -> UP
                RIGHT -> DOWN
            }
            is BackSlashCurve -> when (direction) {
                UP -> LEFT
                DOWN -> RIGHT
                LEFT -> DOWN
                RIGHT -> UP
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
    }
}

object NoCart : Vehicle()

sealed class Path
abstract class Track(open var cart: Vehicle) : Path()
data class VerticalLine(override var cart: Vehicle = NoCart) : Track(cart)
data class HorizontalLine(override var cart: Vehicle = NoCart) : Track(cart)
data class ForwardSlashCurve(override var cart: Vehicle = NoCart) : Track(cart)
data class BackSlashCurve(override var cart: Vehicle = NoCart) : Track(cart)
data class Intersection(override var cart: Vehicle = NoCart) : Track(cart)
object NoTrack : Path()

class TrackMap(lines: List<String>) {
    private val tracks: MutableMap<Point, Path> = mutableMapOf()
    private var carts: MutableSet<Cart> = TreeSet()

    init {
        lines.forEachIndexed { y, line ->
            line.forEachIndexed { x, track ->
                val p = Point(x, y)
                tracks[p] = when (track) {
                    '/' -> ForwardSlashCurve()
                    '\\' -> BackSlashCurve()
                    '-' -> HorizontalLine()
                    '|' -> VerticalLine()
                    '+' -> Intersection()
                    '<' -> processCart(LEFT, p)
                    '>' -> processCart(RIGHT, p)
                    'v' -> processCart(DOWN, p)
                    '^' -> processCart(UP, p)
                    else -> NoTrack
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
                        else -> '#'
                    }
                } else " "
            }

    fun track(p: Point) = if (p.x < 0 || p.y < 0) NoTrack else tracks[p] ?: NoTrack

    private fun processCart(direction: Direction, p: Point): Path {
        val cart = Cart(direction, p)
        carts.add(cart)
        return if (direction in listOf(UP, DOWN)) VerticalLine(cart) else HorizontalLine(cart)
    }

    fun moveCarts(print: Boolean) {
        println(carts)
        if (print) {
            print("\u001b[H\u001b[2J")
            println(toString())
        }
        carts.toCollection(TreeSet()).forEach { cart ->
            val path = track(cart.position)
            if (path is Track) {
                cart.moveForward(this)
                if (carts.zipWithNext().any { (c1, c2) -> c1.position == c2.position })
                    throw CollisionException(cart.position)
                path.cart = NoCart
                (track(cart.position) as? Track)?.cart = cart
            }
        }
    }
}

class CollisionException(val collisionPoint: Point) : Exception()

fun part1(): String {
    val map = TrackMap(File(FILENAME).readLines())
    return try {
        (1..200).forEach { Thread.sleep(0); map.moveCarts(false) }
        "no collisions"
    } catch (ex: CollisionException) {
        "${ex.collisionPoint.x},${ex.collisionPoint.y}"
    }
}

fun part2() = 2