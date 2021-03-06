package com.revtekk.jungle.graph.base

import java.lang.IllegalArgumentException
import java.util.*

/**
 * Adjacency-list based digraph implementation
 *
 * This class implements the digraph methods using a backing adjacency list
 * representation - i.e. A, where i->j exists iff (j in A[ i ])
 */
internal class AdjListDigraph<W>: Digraph<W> {
    private val map = mutableMapOf<Int, MutableSet<Int>>()
    private val edges = mutableMapOf<Pair<Int, Int>, W>()

    override fun addEdge(start: Int, end: Int, weight: W) {
        val set = map.safeGet(start)
        set.add(end)

        edges[start to end] = weight
    }

    override fun removeEdge(start: Int, end: Int): Boolean {
        val set = map.safeGet(start)

        val res = set.remove(end)
        edges.remove(start to end)

        return res
    }

    override fun hasEdge(start: Int, end: Int): Boolean {
        val set = map.safeGet(start)
        return end in set
    }

    override fun getWeight(start: Int, end: Int): W {
        return edges[start to end] ?: throw IllegalArgumentException("no edge from $start to $end")
    }

    override fun vertexSet(): Set<Int> = map.keys
    override fun out(vertex: Int): Set<Int> = map.safeGet(vertex)

    override fun copy(weightCopy: ((W) -> W)?): Digraph<W> {
        val copy = AdjListDigraph<W>()

        // Copy the map entries
        for((key, value) in map) {
            val vCopy = mutableSetOf<Int>()
            vCopy.addAll(value)

            copy.map[key] = vCopy
        }

        // Copy weight mapping
        if(weightCopy != null) {
            for(edge in edges) {
                copy.edges[edge.key.copy()] = weightCopy(edge.value)
            }
        } else {
            copy.edges.putAll(edges)
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

            // Add all the neighbours
            val neigh = map.safeGet(vertex)
            queue.addAll(neigh)

            // Update prev map
            for(w in neigh) {
                if(w == start)
                    continue

                prev.putIfAbsent(w, vertex)
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
        for(neigh in map.safeGet(start)) {
            dfs(neigh, visited, apply)
        }
    }

    /**
     * Private extension function: safeGet()
     *
     * This method calls the MutableMap#getOrPut() method, inserting an empty mutable set if
     * the map does not have the provided key.
     *
     * This eliminates having the code duplication of map.getOrPut(key) { mutableSetOf() } for
     * each time the map lookup is required - instead it is now replaced by map.safeGet(key) which is
     * much cleaner.
     */
    private fun <K, W> MutableMap<K, MutableSet<W>>.safeGet(key: K): MutableSet<W> {
        return getOrPut(key) { mutableSetOf() }
    }
}
