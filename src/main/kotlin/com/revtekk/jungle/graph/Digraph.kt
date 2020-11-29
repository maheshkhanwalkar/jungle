package com.revtekk.jungle.graph

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
}
