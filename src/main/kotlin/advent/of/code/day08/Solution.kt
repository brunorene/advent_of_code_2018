package advent.of.code.day08

import java.io.File

data class Node(
    val children: List<Node>,
    val metadata: List<Int>,
    val level: Int
) {
    val length: Int = 2 + metadata.size + children.map { it.length }.sum()
    val metadataSum: Int = metadata.sum() + children.map { it.metadataSum }.sum()
    val metadataValue: Int = if (children.isEmpty()) metadataSum
    else metadata.map { if (it <= children.size) children[it - 1].metadataValue else 0 }.sum()
}

private val tree = tree(File("day08.txt")
    .readText()
    .trim()
    .split(" ")
    .map { it.toInt() }, 0
)

fun tree(input: List<Int>, level: Int): Node {
    val childCount = input[0]
    val metadataCount = input[1]
    val children = mutableListOf<Node>()
    var childInput = input.drop(2)
    for (s in (1..childCount)) {
        val child = tree(childInput, level + 1)
        children += child
        childInput = childInput.drop(child.length)
    }
    return Node(children, input.drop(2 + children.map { it.length }.sum()).take(metadataCount), level)
}

fun part1() = tree.metadataSum

fun part2() = tree.metadataValue

