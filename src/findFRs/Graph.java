/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package findFRs;

import java.util.*;

/**
 *
 * @author bmumey
 */ 
public class Graph {

    String name;
    int numNodes;
    int[][] neighbor;
    long[] anyNodeStart;
    long maxStart;
    int[] length;
    TreeMap<Integer, TreeSet<Integer>> nodePaths;
    boolean[] containsN;

}
