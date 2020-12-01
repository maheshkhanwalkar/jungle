package com.revtekk.jungle.graph.flow

import com.revtekk.jungle.graph.base.Digraph
import java.util.*

class FlowNetwork(private val graph: Digraph<Int>): Digraph<Int> by graph {
    private val flows = mutableMapOf<Pair<Int, Int>, Int>()

    /**
     * Get the current flow along the specified edge
     * @param start starting vertex
     * @param end ending vertex
     *
     * @return the current flow along the specified edge
     */
    fun getEdgeFlow(start: Int, end: Int): Int {
        return flows.getOrDefault(start to end, 0)
    }

    /**
     * Set the current flow along the specified edge
     * @param start starting vertex
     * @param end ending vertex
     * @param flow specified current flow
     */
    fun setEdgeFlow(start: Int, end: Int, flow: Int) {
        flows[start to end] = flow
    }

    /**
     * Compute the max flow
     *
     * The flow network itself is modified such that after the computation
     * completes, each edge in the network has been updated with the flow
     * along it as needed to achieve the max flow value.
     *
     * @return the max flow value
     */
    fun computeMaxFlow(s: Int, t: Int): Int {
        if(!validate(s, t))
            throw IllegalArgumentException("the current flow is not valid")

        val residual = graph.copy()

        // Adjust initial residual graph to handle a starting non-zero flow
        for((pair, flow) in flows) {
            val start = pair.first
            val end = pair.second

            val capacity = getWeight(start, end)

            if(flow == capacity) {
                residual.removeEdge(start, end)
                residual.addEdge(end, start, flow)
            } else {
                residual.addEdge(start, end, capacity - flow)
                residual.addEdge(end, start, flow)
            }
        }

        while(true) {
            val path = findAugmentingPath(residual, s, t)

            // No s-t path found -- max flow has been achieved
            if(path.isEmpty())
                break

            val flow = computePathFlow(path, residual)

            // Update current flows
            path.zipWithNext().forEach {
                (v, w) -> run {
                    if(graph.hasEdge(v, w)) {
                        setEdgeFlow(v, w, getEdgeFlow(v, w) + flow)
                    } else {
                        setEdgeFlow(v, w, getEdgeFlow(v, w) - flow)
                    }
                }
            }

            // Update the residual network
            updateResidual(residual, path, flow)
        }

        // Compute flow out of s (max flow)
        return graph.out(s).map { v -> getEdgeFlow(s, v) }.reduce { a, b -> a + b}
    }

    /**
     * Check whether the current flow is valid
     *  [1] f(e) >= 0
     *  [2] for all vertices, flow-in(v) = flow-out(v)
     *  [3] flow-out(s) = flow_in(t)
     */
    private fun validate(s: Int, t: Int): Boolean {
        val vertices = graph.vertexSet()

        val inFlow = mutableMapOf<Int, Int>()
        val outFlow = mutableMapOf<Int, Int>()

        for(v in vertices) {
            val out = graph.out(v)
            var totalOut = 0

            for(w in out) {
                val flow = getEdgeFlow(v, w)

                totalOut += flow
                inFlow[w] = inFlow.safeGet(w) + flow
            }

            outFlow[v] = totalOut
        }

        for(v in vertices) {
            // Ignore these -- they don't have conservation of flow
            if(v == s || v == t)
                continue

            if(inFlow.safeGet(v) != outFlow.safeGet(v))
                return false
        }

        // Check [3]
        return outFlow.safeGet(s) == inFlow.safeGet(t)
    }

    /**
     * Find the shortest path from s->t and select it as an augmenting path
     * @param residual residual network
     * @param s source vertex
     * @param t sink vertex
     * @return the short path as a list of vertices along the path (s -> ... -> t)
     */
    private fun findAugmentingPath(residual: Digraph<Int>, s: Int, t: Int): List<Int> {
        val path = LinkedList<Int>()

        // Perform BFS starting from s
        residual.bfs(s) { v, prev -> run {
            if(v == t) {
                var back = v

                // Traverse the prev map to reconstruct the path
                while(back in prev) {
                    path.addFirst(back)
                    back = prev[back] ?: error("vertex not in prev map")
                }

                path.addFirst(back)
            }
        }}

        return path
    }

    /**
     * Compute the maximum flow that can be pushed along the provided augmenting path
     * @param path selected augmenting path
     * @param residual residual network
     * @return the maximum flow
     */
    private fun computePathFlow(path: List<Int>, residual: Digraph<Int>): Int {
        return path.zipWithNext().map {
            (v, w) -> residual.getWeight(v, w)
        }.minOrNull() ?: 0
    }

    /**
     * Update the provided residual network by examining the selected
     * augmenting path and changing/adding/removing edges as needed from the residual network
     *
     * @param residual previous residual network
     * @param path the selected augmenting path
     * @param flow the amount of additional flow being pushed along the provided path
     */
    private fun updateResidual(residual: Digraph<Int>, path: List<Int>, flow: Int) {
        path.zipWithNext().forEach {
            (start, end) -> run {
                val curr = residual.getWeight(start, end)

                // Update forward and backward edges in the residual network
                if(curr == flow) {
                    residual.removeEdge(start, end)
                    residual.addEdge(end, start, flow)
                } else {
                    residual.addEdge(start, end, curr - flow)

                    val revCurr = if(residual.hasEdge(end, start)) residual.getWeight(end, start) else 0
                    residual.addEdge(end, start, revCurr + flow)
                }
            }
        }
    }

    private fun MutableMap<Int, Int>.safeGet(key: Int): Int {
        return getOrPut(key){0}
    }
}
