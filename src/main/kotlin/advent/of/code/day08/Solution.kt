package advent.of.code.day08

import java.io.File

data class Node(
    val children: List<Node>,
    val metadata: List<Int>
) {
    val length: Int = 2 + metadata.size + children.sumBy { it.length }
    val metadataSum: Int = metadata.sum() + children.sumBy { it.metadataSum }
    val metadataValue: Int = if (children.isEmpty()) metadataSum
    else metadata.sumBy { if (it <= children.size) children[it - 1].metadataValue else 0 }
}

private val tree = tree(File("day08.txt")
    .readText()
    .trim()
    .split(" ")
    .map { it.toInt() })

fun tree(input: List<Int>): Node {
    val childCount = input[0]
    val metadataCount = input[1]
    val children = mutableListOf<Node>()
    var childInput = input.drop(2)
    for (s in (1..childCount)) {
        val child = tree(childInput)
        children += child
        childInput = childInput.drop(child.length)
    }
    return Node(children, input.drop(2 + children.sumBy { it.length }).take(metadataCount))
}

fun part1() = tree.metadataSum

fun part2() = tree.metadataValue

