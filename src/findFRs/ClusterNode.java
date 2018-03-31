/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package findFRs;

/**
 *
 * @author bmumey
 */
import java.util.*;
import java.util.concurrent.*;

public class ClusterNode implements Comparable<ClusterNode> {

    int node = -1;
    ClusterNode parent, left, right;
    TreeMap<Integer, int[]> pathLocs;
    int size = 0, fwdSup = 0, rcSup = 0, avgLen = 0;
    
    ConcurrentHashMap<ClusterNode, ClusterEdge> neighbors = null;
   // ClusterNode bestNeighbor = null;
    int bestNsup = 0;
    
    //ArrayList<ClusterEdge> edges;

    public int compareTo(ClusterNode other) {
        int result = Integer.compare(other.fwdSup + other.rcSup, fwdSup + rcSup);
        if (result == 0) {
            result = Integer.compare(other.size, size);
        }
        if (result == 0) {
            result = Integer.compare(getNodeSet().first(), other.getNodeSet().first());
        }
        return result;
    }

    public boolean containsNode(int n) {
        if (node == n) {
            return true;
        } else if (left != null) {
            return left.containsNode(n);
        } else if (right != null) {
            return right.containsNode(n);
        }
        return false;
    }

    int depth() {
        if (parent == null) {
            return 1;
        } else {
            return 1 + parent.depth();
        }
    }
    
    ClusterNode findRoot() {
        if (parent == null)
            return this;
        else return parent.findRoot();
    }

    void addNodes(TreeSet<Integer> ns) {
        if (left == null && right == null) {
            ns.add(node);
        }
        if (left != null) {
            left.addNodes(ns);
        }
        if (right != null) {
            right.addNodes(ns);
        }
    }

    TreeSet<Integer> getNodeSet() {
        TreeSet<Integer> ns = new TreeSet<Integer>();
        this.addNodes(ns);

        return ns;
    }

    synchronized void findPathLocs() {
        if (left != null && left.pathLocs == null) {
            left.findPathLocs();
        }
        if (right != null && right.pathLocs == null) {
            right.findPathLocs();
        }
        pathLocs = new TreeMap<Integer, int[]>();
        TreeSet<Integer> paths = new TreeSet<Integer>();
        if (left != null) {
            paths.addAll(left.pathLocs.keySet());
        }
        if (right != null) {
            paths.addAll(right.pathLocs.keySet());
        }
        //paths.parallelStream().forEach((P) -> {
        for (Integer P : paths) {
            int numlocs = 0;
            if (left.pathLocs.containsKey(P)) {
                numlocs += left.pathLocs.get(P).length;
            }
            if (right.pathLocs.containsKey(P)) {
                numlocs += right.pathLocs.get(P).length;
            }
            int[] arr = new int[numlocs];
            int i = 0;
            if (left.pathLocs.containsKey(P)) {
                for (int x : left.pathLocs.get(P)) {
                    arr[i++] = x;
                }
            }
            if (right.pathLocs.containsKey(P)) {
                for (int x : right.pathLocs.get(P)) {
                    arr[i++] = x;
                }
            }
            Arrays.sort(arr);
            pathLocs.put(P, arr);
        }
    }

//    void addLocs(Map<Integer, ArrayList<Integer>> mergedLocs) {
//        if (pathLocs != null) {
//            pathLocs.keySet().parallelStream().forEach((P) -> {
//                if (!mergedLocs.containsKey(P)) {
//                    mergedLocs.put(P, new ArrayList<Integer>());
//                }
//                for (int i = 0; i < pathLocs.get(P).length; i++) {
//                    mergedLocs.get(P).add(pathLocs.get(P)[i]);
//                }
//            });
////            for (Integer P : pathLocs.keySet()) {
////                if (!mergedLocs.containsKey(P)) {
////                    mergedLocs.put(P, new TreeSet<Integer>());
////                }
////                mergedLocs.get(P).addAll(pathLocs.get(P));
////            }
//        } else {
//            if (left != null) {
//                left.addLocs(mergedLocs);
//            }
//            if (right != null) {
//                right.addLocs(mergedLocs);
//            }
//        }
//    }
}
