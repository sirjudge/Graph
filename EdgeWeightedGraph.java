import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;

/******************************************************************************
 *  Compilation:  javac EdgeWeightedGraph.java
 *  Execution:    java EdgeWeightedGraph filename.txt
 *  Dependencies: Bag.java Edge.java In.java StdOut.java
 *  Data files:   http://algs4.cs.princeton.edu/43mst/tinyEWG.txt
 *                http://algs4.cs.princeton.edu/43mst/mediumEWG.txt
 *                http://algs4.cs.princeton.edu/43mst/largeEWG.txt
 *
 *  An edge-weighted undirected graph, implemented using adjacency lists.
 *  Parallel edges and self-loops are permitted.
 *
 *  % java EdgeWeightedGraph tinyEWG.txt 
 *  8 16
 *  0: 6-0 0.58000  0-2 0.26000  0-4 0.38000  0-7 0.16000  
 *  1: 1-3 0.29000  1-2 0.36000  1-7 0.19000  1-5 0.32000  
 *  2: 6-2 0.40000  2-7 0.34000  1-2 0.36000  0-2 0.26000  2-3 0.17000  
 *  3: 3-6 0.52000  1-3 0.29000  2-3 0.17000  
 *  4: 6-4 0.93000  0-4 0.38000  4-7 0.37000  4-5 0.35000  
 *  5: 1-5 0.32000  5-7 0.28000  4-5 0.35000  
 *  6: 6-4 0.93000  6-0 0.58000  3-6 0.52000  6-2 0.40000
 *  7: 2-7 0.34000  1-7 0.19000  0-7 0.16000  5-7 0.28000  4-7 0.37000
 *
 ******************************************************************************/

/**
 * The {@code EdgeWeightedGraph} class represents an edge-weighted graph of
 * vertices named 0 through <em>V</em> - 1, where each undirected edge is of
 * type {@link Edge} and has a real-valued weight. It supports the following two
 * primary operations: add an edge to the graph, iterate over all of the edges
 * incident to a vertex. It also provides methods for returning the number of
 * vertices <em>V</em> and the number of edges <em>E</em>. Parallel edges and
 * self-loops are permitted.
 * <p>
 * This implementation uses an adjacency-lists representation, which is a
 * vertex-indexed array of @link{Bag} objects. All operations take constant time
 * (in the worst case) except iterating over the edges incident to a given
 * vertex, which takes time proportional to the number of such edges.
 * <p>
 * For additional documentation, see
 * <a href="http://algs4.cs.princeton.edu/43mst">Section 4.3</a> of
 * <i>Algorithms, 4th Edition</i> by Robert Sedgewick and Kevin Wayne.
 *
 * @author Robert Sedgewick
 * @author Kevin Wayne
 */

public class EdgeWeightedGraph {
	private static final String NEWLINE = System.getProperty("line.separator");

	private final int V;
	private int E;
	private Bag<Edge>[] adj;
	private ArrayList<KrusNode> set;
	private ArrayList<Edge> edgeySet;
	private ArrayList<Edge> edgesLookedAt;

	/**
	 * Initializes an empty edge-weighted graph with {@code V} vertices and 0
	 * edges.
	 *
	 * @param V
	 *            the number of vertices
	 * @throws IllegalArgumentException
	 *             if {@code V < 0}
	 */
	public EdgeWeightedGraph(int V) {
		if (V < 0)
			throw new IllegalArgumentException("Number of vertices must be nonnegative");
		this.V = V;
		this.E = 0;
		edgeySet = new ArrayList<>();
		set = new ArrayList<>();
		edgesLookedAt = new ArrayList<>();
		adj = (Bag<Edge>[]) new Bag[V];
		for (int v = 0; v < V; v++) {
			adj[v] = new Bag<Edge>();
		}
	}

	/**
	 * Initializes an edge-weighted graph from an input stream. The format is
	 * the number of vertices <em>V</em>, followed by the number of edges
	 * <em>E</em>, followed by <em>E</em> pairs of vertices and edge weights,
	 * with each entry separated by whitespace.
	 *
	 * @param in
	 *            the input stream
	 * @throws IndexOutOfBoundsException
	 *             if the endpoints of any edge are not in prescribed range
	 * @throws IllegalArgumentException
	 *             if the number of vertices or edges is negative
	 */
	public EdgeWeightedGraph(In in) {
		this(in.readInt());
		int E = in.readInt();
		if (E < 0)
			throw new IllegalArgumentException("Number of edges must be nonnegative");
		for (int i = 0; i < E; i++) {
			int v = in.readInt();
			int w = in.readInt();
			double weight = in.readDouble();
			Edge e = new Edge(v, w, weight);
			addEdge(e);
		}
	}

	/**
	 * Initializes a new edge-weighted graph that is a deep copy of {@code G}.
	 *
	 * @param G
	 *            the edge-weighted graph to copy
	 */
	public EdgeWeightedGraph(EdgeWeightedGraph G) {
		this(G.V());
		this.E = G.E();
		for (int v = 0; v < G.V(); v++) {
			// reverse so that adjacency list is in same order as original
			Stack<Edge> reverse = new Stack<Edge>();
			for (Edge e : G.adj[v]) {
				reverse.push(e);
			}
			for (Edge e : reverse) {
				adj[v].add(e);
			}
		}
	}

	/**
	 * Returns the number of vertices in this edge-weighted graph.
	 *
	 * @return the number of vertices in this edge-weighted graph
	 */
	public int V() {
		return V;
	}

	/**
	 * Returns the number of edges in this edge-weighted graph.
	 *
	 * @return the number of edges in this edge-weighted graph
	 */
	public int E() {
		return E;
	}

	// throw an IndexOutOfBoundsException unless {@code 0 <= v < V}
	private void validateVertex(int v) {
		if (v < 0 || v >= V)
			throw new IndexOutOfBoundsException("vertex " + v + " is not between 0 and " + (V - 1));
	}

	/**
	 * Adds the undirected edge {@code e} to this edge-weighted graph.
	 *
	 * @param e
	 *            the edge
	 * @throws IndexOutOfBoundsException
	 *             unless both endpoints are between {@code 0} and {@code V-1}
	 */
	public void addEdge(Edge e) {
		int v = e.either();
		int w = e.other(v);
		validateVertex(v);
		validateVertex(w);
		adj[v].add(e);
		adj[w].add(e);
		E++;
	}

	/**
	 * Returns the edges incident on vertex {@code v}.
	 *
	 * @param v
	 *            the vertex
	 * @return the edges incident on vertex {@code v} as an Iterable
	 * @throws IndexOutOfBoundsException
	 *             unless {@code 0 <= v < V}
	 */
	public Iterable<Edge> adj(int v) {
		validateVertex(v);
		return adj[v];
	}

	/**
	 * Returns the degree of vertex {@code v}.
	 *
	 * @param v
	 *            the vertex
	 * @return the degree of vertex {@code v}
	 * @throws IndexOutOfBoundsException
	 *             unless {@code 0 <= v < V}
	 */
	public int degree(int v) {
		validateVertex(v);
		return adj[v].size();
	}

	/**
	 * Returns all edges in this edge-weighted graph. To iterate over the edges
	 * in this edge-weighted graph, use foreach notation:
	 * {@code for (Edge e : G.edges())}.
	 *
	 * @return all edges in this edge-weighted graph, as an iterable
	 */
	public Iterable<Edge> edges() {
		Bag<Edge> list = new Bag<Edge>();
		for (int v = 0; v < V; v++) {
			int selfLoops = 0;
			for (Edge e : adj(v)) {
				if (e.other(v) > v) {
					list.add(e);
				}
				// only add one copy of each self loop (self loops will be
				// consecutive)
				else if (e.other(v) == v) {
					if (selfLoops % 2 == 0)
						list.add(e);
					selfLoops++;
				}
			}
		}
		return list;
	}

	/**
	 * Returns a string representation of the edge-weighted graph. This method
	 * takes time proportional to <em>E</em> + <em>V</em>.
	 *
	 * @return the number of vertices <em>V</em>, followed by the number of
	 *         edges <em>E</em>, followed by the <em>V</em> adjacency lists of
	 *         edges
	 */
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append(V + " " + E + NEWLINE);
		for (int v = 0; v < V; v++) {
			s.append(v + ": ");
			for (Edge e : adj[v]) {
				s.append(e + "  ");
			}
			s.append(NEWLINE);
		}
		return s.toString();
	}

	public void makeSet(int x) {
		KrusNode kNode = new KrusNode();
		set.add(kNode);
	}

	public KrusNode getParentNode(int x) {
		KrusNode kNode = set.get(x); // get the node from the set
		if (kNode.getParentNode().equals(kNode))
			return kNode; // if the parent node is the node (it is the root
							// node) return that node
		else
			return kNode.getParentNode();
	}

	public KrusNode getParentNode(KrusNode krusIn) {
		if (krusIn.getParentNode().equals(krusIn))
			return krusIn; // if the parent node is the node (it is the root
							// node) return that node
		else
			return krusIn.getParentNode();
	}

	public KrusNode getRootNode(KrusNode kNode){
		if(kNode.getParentNode().equals(kNode)) return kNode;
		else return getRootNode(kNode.getParentNode());
	}

	
	
	
	public void union(KrusNode k1, KrusNode k2) {
		if (k1.getSize() > k2.getSize()) {
			k2.setParentNode(k1.getParentNode());
			k2.plusOneSize();
		} else {
			k1.setParentNode(k2.getParentNode());
			k1.plusOneSize();
		}
	}

	public void printSet() {
		System.out.print("[");
		for (KrusNode n : set) {
			System.out.print(n + ",");
		}
		System.out.println("]");
	}

	public void printEdgeySet() {
		System.out.print("[");
		for (Edge e : edgeySet) {
			System.out.print(e + ",");
		}
		System.out.println("]");
	}

	public void kruskal() {
		for (Bag<Edge> b : adj) {
			for (Edge e : b) {
				edgeySet.add(e);
			}
		}
		Collections.sort(edgeySet);// sort the bag of edges

		for (Edge e : edgeySet) { // go through list of edges, initialize the set of verticies
			int v = e.getV(); // get each vertex
			int w = e.getW();
			makeSet(v);
			makeSet(w);
		}

		for (Edge e : edgeySet) { // loop through again, calls findSet on each vertex
			int v = e.getV(); // get each vertex
			int w = e.getW();
			
			// find the root node for each vertex
			KrusNode pNodeV = getRootNode(getParentNode(v));
			KrusNode pNodeW = getRootNode(getParentNode(w));
			
			// if they have the same root node, it's a cycle, don't print
			//System.out.println("I am checking: " + e);
			if (pNodeV != pNodeW) {
				// if we have not already looked at the edge in the set
				if (!edgesLookedAt.contains(e)) {
					edgesLookedAt.add(e); // add it to the edgesLookedAt
					System.out.println(e); // print out the edge
					union(pNodeV, pNodeW);
				}
			}
		}
	}

	/**
	 * Unit tests the {@code EdgeWeightedGraph} data type.
	 *
	 * @param args
	 *            the command-line arguments
	 */

	public static void main(String[] args) {
		In in = new In(args[0]);
		EdgeWeightedGraph G = new EdgeWeightedGraph(in);
		StdOut.println(G);
		G.kruskal();
	}

}
