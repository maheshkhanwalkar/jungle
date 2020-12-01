package com.revtekk.jungle.graph.base

interface Digraph<W> {
    /**
     * Add an edge to the graph
     * @param start starting vertex
     * @param end ending vertex
     * @param weight edge weight
     */
    fun addEdge(start: Int, end: Int, weight: W)

    /**
     * Remove an edge from the graph (if it exists)
     * @param start starting vertex
     * @param end ending vertex
     *
     * @return true if the edge exists, false otherwise
     */
    fun removeEdge(start: Int, end: Int): Boolean

    /**
     * Check whether there exists an edge from start->end
     * @param start starting vertex
     * @param end ending vertex
     *
     * @return true if start->end exists, false otherwise
     */
    fun hasEdge(start: Int, end: Int): Boolean

    /**
     * Get the edge weight for start->end
     * @param start starting vertex
     * @param end ending vertex
     *
     * @return the edge weight
     */
    fun getWeight(start: Int, end: Int): W

    /**
     * Get the set of vertices
     * @return the vertex set
     */
    fun vertexSet(): Set<Int>

    /**
     * Get the set of outgoing vertices from the provided vertex
     * @param vertex provided vertex to examine
     * @return the set of outgoing vertices
     */
    fun out(vertex: Int): Set<Int>

    /**
     * Create a copy of the current graph
     *
     * This is not a lightweight copy -- but rather a deep copy, so the
     * underlying data structures and recreated and then brought to the
     * equivalent state.
     *
     * @param weightCopy optional function to perform a weight object deep copy
     */
    fun copy(weightCopy: ((W) -> W)? = null): Digraph<W>

    /**
     * Perform a BFS (breadth-first search) traversal on the graph
     *
     * This method does not return anything, nor does it accept a function
     * which returns anything. Therefore, if a custom function needs to return
     * anything, it should track that within a captured variable.
     *
     * @param start starting vertex of the BFS traversal
     * @param apply function to call for each traversed vertex
     */
    fun bfs(start: Int, apply: (vertex: Int, prev: Map<Int, Int>) -> Unit)

    /**
     * Perform a DFS (depth-first search) traversal on the graph
     *
     * This method does not return anything, nor does it accept a function
     * which returns anything. Therefore, if a custom function needs to return
     * anything, it should track that within a captured variable.
     *
     * @param start starting vertex of the DFS traversal
     * @param apply function to call for each traversed vertex
     */
    fun dfs(start: Int, apply: (vertex: Int) -> Unit)
}
