package advent.of.code.day13

import advent.of.code.day13.Direction.*
import advent.of.code.day13.Forward.*
import java.io.BufferedWriter
import java.io.File
import java.util.*

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

sealed class Path(open val touches: List<Direction>)
abstract class Track(open var cart: Vehicle, override val touches: List<Direction>) : Path(touches)
data class VerticalLine(override var cart: Vehicle = NoCart) : Track(cart, listOf(UP, DOWN)) {
    override val touches: List<Direction>
        get() = super.touches
}

data class HorizontalLine(override var cart: Vehicle = NoCart) : Track(cart, listOf(LEFT, RIGHT)) {
    override val touches: List<Direction>
        get() = super.touches
}

data class UpRightCurve(override var cart: Vehicle = NoCart) : Track(cart, listOf(UP, RIGHT)) {
    override val touches: List<Direction>
        get() = super.touches
}

data class UpLeftCurve(override var cart: Vehicle = NoCart) : Track(cart, listOf(UP, LEFT)) {
    override val touches: List<Direction>
        get() = super.touches
}

data class DownLeftCurve(override var cart: Vehicle = NoCart) : Track(cart, listOf(DOWN, LEFT)) {
    override val touches: List<Direction>
        get() = super.touches
}

data class DownRightCurve(override var cart: Vehicle = NoCart) : Track(cart, listOf(DOWN, RIGHT)) {
    override val touches: List<Direction>
        get() = super.touches
}

data class Intersection(override var cart: Vehicle = NoCart) : Track(cart, listOf(UP, DOWN, LEFT, RIGHT)) {
    override val touches: List<Direction>
        get() = super.touches
}

object NoTrack : Path(listOf()) {
    override val touches: List<Direction>
        get() = super.touches
}

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
        tracks.keys.sortedWith(compareBy({ it.y }, { it.x })).map { p ->
            val path = track(p)
            val pixel = if (p.x == 0) "\n" else ""
            pixel + if (cartPositions.contains(p) && path is Track) {
                val cart = path.cart
                if (cart is Cart)
                    when (cart.direction) {
                        UP -> '^'
                        DOWN -> 'v'
                        LEFT -> '<'
                        RIGHT -> '>'
                    }
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
        }.joinToString("")

    fun track(p: Point) = if (p.x < 0 || p.y < 0) NoTrack else tracks[p] ?: NoTrack

    private fun up(p: Point) = track(p.copy(y = p.y - 1))
    private fun down(p: Point) = track(p.copy(y = p.y + 1))
    private fun left(p: Point) = track(p.copy(x = p.x - 1))
    private fun right(p: Point) = track(p.copy(x = p.x + 1))

    private fun processCart(direction: Direction, p: Point): Path {
        cartPositions.add(p)
        cartPositions.sortWith(compareBy({ it.y }, { it.x }))
        val touchesUp = DOWN in up(p).touches
        val touchesDown = UP in down(p).touches
        val touchesRight = LEFT in right(p).touches
        val touchesLeft = RIGHT in left(p).touches
        return if (touchesUp && touchesDown)
            if (touchesLeft && touchesRight) Intersection(Cart(direction))
            else VerticalLine(Cart(direction))
        else if (touchesLeft && touchesRight) HorizontalLine(Cart(direction))
        else if (touchesLeft && touchesDown) DownLeftCurve(Cart(direction))
        else if (touchesRight && touchesDown) DownRightCurve(Cart(direction))
        else if (touchesRight && touchesUp) UpRightCurve(Cart(direction))
        else if (touchesLeft && touchesUp) UpRightCurve(Cart(direction))
        else NoTrack
    }

    fun moveCarts(writer: BufferedWriter) {
        writer.write(toString())
        writer.newLine()
        writer.newLine()
        cartPositions = cartPositions.map { cartPos ->
            print("$cartPos ")
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
        println()
    }
}

class CollisionException(val collisionPoint: Point) : Exception()

fun part1(): String {
    val map = TrackMap(File(FILENAME).readLines())
    return try {
        File("result.txt").bufferedWriter().use { writer ->
            (1..20000).forEach { map.moveCarts(writer) }
        }
        "no collisions"
    } catch (ex: CollisionException) {
        "${ex.collisionPoint.x},${ex.collisionPoint.y}"
    }
}

fun part2() = 2