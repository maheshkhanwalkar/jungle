package com.revtekk.jungle.graph.base

import java.lang.IllegalArgumentException
import java.util.*

/**
 * Adjacency-matrix based digraph implementation
 *
 * This class implements the digraph methods using a backing adjacency matrix
 * representation - i.e. A, where A[i, j] = 0 iff there does not exist an edge i->j
 */
internal class AdjMatrixDigraph<W>(num: Int): Digraph<W> {
    private val matrix: Array<IntArray> = Array(num) { IntArray(num) }
    private val edges = mutableMapOf<Pair<Int, Int>, W>()
    private val vSet = (0 until num).toSet()

    override fun addEdge(start: Int, end: Int, weight: W) {
        matrix[start][end] = 1
        edges[start to end] = weight
    }

    override fun removeEdge(start: Int, end: Int): Boolean {
        matrix[start][end] = 0
        return edges.remove(start to end) != null
    }

    override fun hasEdge(start: Int, end: Int): Boolean {
        return matrix[start][end] == 1
    }

    override fun getWeight(start: Int, end: Int): W {
        return edges[start to end] ?: throw IllegalArgumentException("no edge from $start to $end")
    }

    override fun vertexSet(): Set<Int> = vSet

    override fun out(vertex: Int): Set<Int> =
        matrix[vertex].mapIndexed {
            index, elem -> index to elem
        }.filter { it.second != 0 }.map {
            (index, _) -> index
        }.toSet()

    override fun copy(weightCopy: ((W) -> W)?): Digraph<W> {
        val copy = AdjMatrixDigraph<W>(matrix.size)

        // Copy weight mapping
        if(weightCopy != null) {
            for(edge in edges) {
                copy.edges[edge.key.copy()] = weightCopy(edge.value)
            }
        } else {
            copy.edges.putAll(edges)
        }

        // Copy adjacency matrix
        for(i in matrix.indices) {
            for(j in matrix.indices) {
                if(weightCopy != null) {
                    copy.matrix[i][j] = matrix[i][j]
                }

                copy.matrix[i][j] = matrix[i][j]
            }
        }

        return copy
    }

    override fun bfs(start: Int, apply: (vertex: Int, prevMap: Map<Int, Int>) -> Unit) {
        val visited = mutableSetOf<Int>()
        val queue = LinkedList<Int>()
        val prev = mutableMapOf<Int, Int>()

        queue.add(start)

        while(!queue.isEmpty()) {
            val vertex = queue.remove()

            if(vertex in visited)
                continue

            // Mark vertex as visited
            visited.add(vertex)

            // Perform custom action
            apply(vertex, prev)

            // Add all the neighbours and update prev map
            for(i in matrix[vertex].indices) {
                if(matrix[vertex][i] != 0) {
                    queue.add(i)

                    if(i != start) {
                        prev.putIfAbsent(i, vertex)
                    }
                }
            }
        }
    }

    override fun dfs(start: Int, apply: (vertex: Int) -> Unit) {
        return dfs(start, mutableSetOf(), apply)
    }

    /**
     * Internal DFS implementation (recursive)
     */
    private fun dfs(start: Int, visited: MutableSet<Int>, apply: (vertex: Int) -> Unit) {
        if(start in visited)
            return

        // Mark vertex as visited
        visited.add(start)

        // Perform custom action
        apply(start)

        // Perform DFS starting at each neighbour
        for(i in matrix[start].indices) {
            if(matrix[start][i] != 0)
                dfs(i, visited, apply)
        }
    }
}
