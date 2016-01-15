package algorithm;
// Created: 23.04.2015, Ali Batuhan Yardým

import java.util.ArrayList;

import org.graphstream.graph.*;

// a base class for the classes containing algorithms that search and advance
// the graph of the geometric system
public abstract class GeoSearcher {
	// search based on a single target node
	// toProcessed specifies the list of points that will be
	// processed in the next iteration, the seacrher can add new
	// points to be searched
	public abstract void processNode ( Node n, ArrayList<Node> toBeProcessed);
}