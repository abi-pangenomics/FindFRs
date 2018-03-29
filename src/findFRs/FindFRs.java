package findFRs;

/**
 *
 * @author bmumey
 */
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.io.*;
import java.util.stream.IntStream;

public class FindFRs {

    static Graph g;
    static ArrayList<Sequence> sequences;
    static char[] fastaConcat;
    static int fastaConcatLen;
    static TreeMap<Long, Integer> startToNode;
    static int[][] paths;

    static PriorityQueue<ClusterEdge> edgeQ;
    static ArrayList<ClusterEdge> edgeL;

    static PriorityQueue<ClusterNode> iFRQ;
    //static ClusterNode[] startNode;

    static ConcurrentHashMap<Integer, ClusterNode> nodeCluster;

    // command line arguments:
    static String dotFile = ""; // .dot filename
    static String fastaFile = ""; // .fasta filename
    static int K = -1; // k-mer size
    static double alpha = 0.7; // epsilon_r parameter
    static int kappa = 0; // maxInsert parameter
    static int minSup;
    static int minLen;
    static boolean useRC; // indicates if fasta file was appended with its reverse-complement
    //static String filePrefix = ""; // file name prefix

    static String[] colors = {"122,39,25", "92,227,60", "225,70,233", "100,198,222", "232,176,49", "50,39,85",
        "67,101,33", "222,142,186", "92,119,227", "206,225,151", "227,44,118", "229,66,41",
        "47,36,24", "225,167,130", "120,132,131", "104,232,178", "158,43,133", "228,228,42", "213,217,213",
        "118,64,79", "88,155,219", "226,118,222", "146,197,53", "222,100,89", "224,117,41", "160,96,228",
        "137,89,151", "126,209,119", "145,109,70", "91,176,164", "54,81,103", "164,174,137", "172,166,48",
        "56,86,143", "210,184,226", "175,123,35", "129,161,88", "158,47,85", "87,231,225", "216,189,112", "49,111,75",
        "89,137,168", "209,118,134", "33,63,44", "166,128,142", "53,137,55", "80,76,161", "170,124,221", "57,62,13",
        "176,40,40", "94,179,129", "71,176,51", "223,62,170", "78,25,30", "148,69,172", "122,105,31", "56,33,53",
        "112,150,40", "239,111,176", "96,55,25", "107,90,87", "164,74,28", "171,198,226", "152,131,176", "166,225,211",
        "53,121,117", "220,58,86", "86,18,56", "225,197,171", "139,142,217", "216,151,223", "97,229,117", "225,155,85",
        "31,48,58", "160,146,88", "185,71,129", "164,233,55", "234,171,187", "110,97,125", "177,169,175", "177,104,68",
        "97,48,122", "237,139,128", "187,96,166", "225,90,127", "97,92,55", "124,35,99", "210,64,194", "154,88,84",
        "100,63,100", "140,42,54", "105,132,99", "186,227,103", "224,222,81", "191,140,126", "200,230,182", "166,87,123",
        "72,74,58", "212,222,124", "205,52,136"};
    static String[] svgcolors = {"aliceblue", "antiquewhite", "aqua", "aquamarine",
        "azure", "beige", "bisque", "black", "blanchedalmond", "blue",
        "blueviolet", "brown", "burlywood", "cadetblue", "chartreuse",
        "chocolate", "coral", "cornflowerblue", "cornsilk", "crimson",
        "cyan", "darkblue", "darkcyan", "darkgoldenrod", "darkgray",
        "darkgreen", "darkgrey", "darkkhaki", "darkmagenta", "darkolivegreen",
        "darkorange", "darkorchid", "darkred", "darksalmon", "darkseagreen",
        "darkslateblue", "darkslategray", "darkslategrey", "darkturquoise", "darkviolet",
        "deeppink", "deepskyblue", "dimgray", "dimgrey", "dodgerblue",
        "firebrick", "floralwhite", "forestgreen", "fuchsia", "gainsboro",
        "ghostwhite", "gold", "goldenrod", "gray", "grey",
        "green", "greenyellow", "honeydew", "hotpink", "indianred",
        "indigo", "ivory", "khaki", "lavender", "lavenderblush",
        "lawngreen", "lemonchiffon", "lightblue", "lightcoral", "lightcyan",
        "lightgoldenrodyellow", "lightgray", "lightgreen", "lightgrey", "lightpink",
        "lightsalmon", "lightseagreen", "lightskyblue", "lightslategray", "lightslategrey",
        "lightsteelblue", "lightyellow", "lime", "limegreen", "linen",
        "magenta", "maroon", "mediumaquamarine", "mediumblue", "mediumorchid",
        "mediumpurple", "mediumseagreen", "mediumslateblue", "mediumspringgreen", "mediumturquoise",
        "mediumvioletred", "midnightblue", "mintcream", "mistyrose", "moccasin",
        "navajowhite", "navy", "oldlace", "olive", "olivedrab",
        "orange", "orangered", "orchid", "palegoldenrod", "palegreen",
        "paleturquoise", "palevioletred", "papayawhip", "peachpuff", "peru",
        "pink", "plum", "powderblue", "purple", "red",
        "rosybrown", "royalblue", "saddlebrown", "salmon", "sandybrown",
        "seagreen", "seashell", "sienna", "silver", "skyblue",
        "slateblue", "slategray", "slategrey", "snow", "springgreen",
        "steelblue", "tan", "teal", "thistle", "tomato",
        "turquoise", "violet", "wheat", "white", "whitesmoke",
        "yellow", "yellowgreen"};

    static void readData() {
        g = ReadInput.readDotFile(dotFile);

        startToNode = new TreeMap<Long, Integer>();
        for (int i = 0; i < g.starts.length; i++) {
            for (int j = 0; j < g.starts[i].length; j++) {
                startToNode.put(g.starts[i][j], i);
            }
            long firstStart = g.starts[i][0];
            g.starts[i] = new long[1]; // only save 1st start
            g.starts[i][0] = firstStart;
        }

        sequences = ReadInput.readFastaFile(fastaFile);
    }

    static char findFastaConcat(long[] seqStart, long index) {
        int i = 0;
//        while (i < seqStart.length - 1 && seqStart[i + 1] <= index) {
//            i++;
//        }
        int min = 0;
        int max = seqStart.length - 1;
        int mid = (min + max + 1) / 2;
        while (min < max) {
            if (seqStart[mid] < index) {
                min = mid;
                mid = (min + max + 1) / 2;

            } else if (seqStart[mid] == index) {
                min = max = mid;
            } else if (seqStart[mid] > index) {
                max = mid - 1;
                mid = (min + max + 1) / 2;
            }
        }
//        if (mid != i) {
//            System.out.println("here");
//        }
        i = mid;
        if (index - seqStart[i] == sequences.get(i).seq.length()) {
            return '$';
        }

        return sequences.get(i).seq.charAt((int) (index - seqStart[i]));

    }

    static void buildPaths() {
        ArrayList<ArrayList<Integer>> pathsAL = new ArrayList<ArrayList<Integer>>();
        long curStart = 1;
        long seqStart[] = new long[sequences.size()];
        int index = 0;
        seqStart[0] = 1;
        long seqEnd;
        //long prevStart = 0;
        for (Sequence s : sequences) {
            ArrayList path = new ArrayList<Integer>();
            s.startPos = seqStart[index];
            s.length = s.seq.length();
            seqEnd = seqStart[index] + s.length - 1;
            curStart = seqStart[index];
            while (curStart > 0 && !startToNode.containsKey(curStart)) {
                curStart--;
            }
            path.add(startToNode.get(curStart));
            do {
                curStart += g.length[startToNode.get(curStart)] - (K - 1);
                if (startToNode.containsKey(curStart)) {
                    path.add(startToNode.get(curStart));
                }
            } while (startToNode.containsKey(curStart) && curStart + g.length[startToNode.get(curStart)] - 1 < seqEnd);

            pathsAL.add(path);
            if (index < sequences.size() - 1) {
                seqStart[++index] = seqEnd + 2;
            }
            //fastaConcatLen += 1 + s.seq.length();
        }
//        fastaConcatLen++;
//        fastaConcat = new char[fastaConcatLen];
//        for (Sequence s : sequences) {
//            fastaConcat[s.startPos - 1] = '$';
//            for (int i = 0; i < s.length; i++) {
//                fastaConcat[s.startPos + i] = s.seq.charAt(i);
//            }
//            s.seq = null; // no longer needed
//        }
//        fastaConcat[fastaConcat.length - 1] = '$';
        //System.out.println(Arrays.toString(fastaConcat));
        System.out.println("number of paths: " + pathsAL.size());

        paths = new int[pathsAL.size()][];
        for (int i = 0; i < pathsAL.size(); i++) {
            ArrayList<Integer> path = pathsAL.get(i);
            paths[i] = new int[path.size()];
            for (int j = 0; j < path.size(); j++) {
                paths[i][j] = path.get(j);
            }
        }

        pathsAL.clear();
        pathsAL = null; // can be gc'ed

        System.out.println("finding node paths");

        g.containsN = new boolean[g.numNodes];
        for (int i = 0; i < g.numNodes; i++) {
            g.containsN[i] = false;
            for (int j = 0; j < g.length[i]; j++) {
                if (findFastaConcat(seqStart, g.starts[i][0] + j) == 'N') {
//                        || findFastaConcat(seqStart, g.starts[i][0] + j) == '$') {
                    g.containsN[i] = true;
                    //System.out.println("node " + i + " contains N; ignored for FRs");
                    break;
                }
            }
        }

        // find paths for each node:
        g.nodePaths = new TreeMap<Integer, TreeSet<Integer>>();
        for (int i = 0; i < g.numNodes; i++) {
            if (!g.containsN[i]) {
                g.nodePaths.put(i, new TreeSet<Integer>());
            }
        }

        for (int i = 0; i < paths.length; i++) {
            for (int j = 0; j < paths[i].length; j++) {
                if (!g.containsN[paths[i][j]]) {
                    g.nodePaths.get(paths[i][j]).add(i);
                }
            }
        }

    }

    static int gap(int[] path, int start, int stop) {
        int curStartLoc = 1;
        int curEndLoc = 1;
        for (int i = start; i <= stop; i++) {
            curEndLoc = curStartLoc + g.length[path[i]] - 1;
            curStartLoc += g.length[path[i]] - (K - 1);
        }
        int gp = curEndLoc - g.length[path[start]] - g.length[path[stop]];
        if (gp <= 0) {
            return Integer.MAX_VALUE;
        }
        return gp;
    }

    static ConcurrentLinkedQueue<PathSegment> computeSupport(ClusterNode clust, boolean createPSList, boolean findAvgLen) {
        if (clust.pathLocs == null) {
            clust.findPathLocs();
        }
        ConcurrentLinkedQueue<PathSegment> segList = new ConcurrentLinkedQueue<PathSegment>();
        AtomicInteger fSup = new AtomicInteger(0);
        AtomicInteger rSup = new AtomicInteger(0);
        AtomicInteger supLen = new AtomicInteger(0);
        clust.pathLocs.keySet().parallelStream().forEach((P) -> {
            int[] locs = clust.pathLocs.get(P);
            int start = 0;
            while (start < locs.length) {
                int last = start;
                while (last + 1 < locs.length
                        && ((locs[last + 1] == locs[last] + 1)
                        || (kappa > 0 && gap(paths[P], locs[last], locs[last + 1]) <= kappa))) {
                    last++;
                }
                if (last - start + 1 >= alpha * clust.size) {
                    if (!useRC || P < paths.length / 2) {
                        fSup.incrementAndGet();
                    }
                    if (useRC && P >= paths.length / 2) {
                        rSup.incrementAndGet();
                    }
                    if (createPSList) {
                        segList.add(new PathSegment(P, locs[start], locs[last]));
                    }
                    if (findAvgLen) {
                        long[] startStop = findFastaLoc(P, locs[start], locs[last]);
                        int len = (int) (startStop[1] - startStop[0]); // last pos is exclusive
                        supLen.getAndAdd(len);
                    }
                }
                start = last + 1;
            }
        });
        clust.fwdSup = fSup.get();
        clust.rcSup = rSup.get();
        if (findAvgLen && clust.fwdSup + clust.rcSup > 0) {
            clust.avgLen = supLen.get() / (clust.fwdSup + clust.rcSup);
        }
        if (createPSList) {
            return segList;
        }
        return null;
    }

    static void finalizeEdge(ClusterEdge e) {
        ClusterNode newRoot = new ClusterNode();
        newRoot.nodeNum = numClusterNodes++;
        newRoot.left = e.u;
        newRoot.right = e.v;
        newRoot.parent = null;
        newRoot.pathLocs = null;
        newRoot.edges = new ArrayList<ClusterEdge>();
        newRoot.size = newRoot.left.size + newRoot.right.size;
        newRoot.left.parent = newRoot;
        newRoot.right.parent = newRoot;
        newRoot.neighbors = new TreeSet<ClusterNode>();

//        computeSupport(newRoot, false, false);
//
//        //System.out.println("merging: left size: " + e.u.size + " right size: " + e.v.size + " support: " + e.potentialSup);
//        TreeSet<ClusterNode> neighbors = new TreeSet<ClusterNode>();
//        for (ClusterEdge ce : e.u.edges) {
//            ClusterNode n = ce.other(e.u);
//            if (n != e.v && n.parent == null) {
//                neighbors.add(n);
//            }
//            //edgeQ.remove(ce);
//            //n.edges.remove(ce);
//        }
//        for (ClusterEdge ce : e.v.edges) {
//            ClusterNode n = ce.other(e.v);
//            if (n != e.u && n.parent == null) {
//                neighbors.add(n);
//            }
//            //edgeQ.remove(ce);
//            //n.edges.remove(ce);
//        }
//        e.u.edges.clear();
//        e.v.edges.clear();
//        for (ClusterNode n : neighbors) {
//            ClusterNode tmpClst = new ClusterNode();
//            tmpClst.left = newRoot;
//            tmpClst.right = n;
//            tmpClst.parent = null;
//            tmpClst.size = tmpClst.left.size + tmpClst.right.size;
//            computeSupport(tmpClst, false, false);
//            tmpClst.pathLocs.clear();
//            ClusterEdge newE = new ClusterEdge(tmpClst.left, tmpClst.right, tmpClst.fwdSup + tmpClst.rcSup);
//            newRoot.edges.add(newE);
//            n.edges.add(newE);
//            //edgeQ.add(newE);
//        }
    }

    static int numClusterNodes = 0;

    static void findFRs() {
        System.out.println("creating node clusters");
        nodeCluster = new ConcurrentHashMap<Integer, ClusterNode>(g.numNodes);

        // create initial node clusters
        g.nodePaths.keySet().parallelStream().forEach((N) -> {
            if (!g.nodePaths.get(N).isEmpty()
                    && (!useRC || g.nodePaths.get(N).first() < paths.length / 2)) { // only start with nodes from non-rc'ed paths
                ClusterNode nodeClst = new ClusterNode();
                nodeClst.parent = nodeClst.left = nodeClst.right = null;
                //nodeClst.node = N;
                nodeClst.nodeNum = numClusterNodes++;
                nodeClst.size = 1;
                nodeClst.pathLocs = new ConcurrentHashMap<Integer, int[]>();
                nodeClst.edges = new ArrayList<ClusterEdge>();
                nodeClst.neighbors = new TreeSet<ClusterNode>();
                nodeCluster.put(N, nodeClst);
            }
        });
        g.nodePaths.clear(); //not used after this
        g.nodePaths = null;
        TreeMap<Integer, TreeMap<Integer, ArrayList<Integer>>> nodePathLocs
                = new TreeMap<Integer, TreeMap<Integer, ArrayList<Integer>>>();
        for (int p = 0; p < paths.length; p++) {
            for (int i = 0; i < paths[p].length; i++) {
                int n = paths[p][i];
                if (!nodePathLocs.containsKey(n)) {
                    nodePathLocs.put(n, new TreeMap<Integer, ArrayList<Integer>>());
                }
                if (!nodePathLocs.get(n).containsKey(p)) {
                    nodePathLocs.get(n).put(p, new ArrayList<Integer>());
                }
                nodePathLocs.get(n).get(p).add(i);
            }
        }
        for (Integer node : nodePathLocs.keySet()) {
            nodePathLocs.get(node).keySet().parallelStream().forEach((P) -> {
                ArrayList<Integer> al = nodePathLocs.get(node).get(P);
                int[] ar = new int[al.size()];
                int i = 0;
                for (Integer x : al) {
                    ar[i++] = x;
                }
                if (nodeCluster.containsKey(node)) {
                    nodeCluster.get(node).pathLocs.put(P, ar);
                }
            });
        }
        nodePathLocs.clear();

        System.out.println("computing node support");
        for (ClusterNode c : nodeCluster.values()) {
            computeSupport(c, false, false);
        }
        // create initial edges
        edgeQ = new PriorityQueue<ClusterEdge>();
        edgeL = new ArrayList<ClusterEdge>();
        TreeSet<ClusterNode> checkNodes = new TreeSet<ClusterNode>();

        System.out.println("adding neighbors");
        for (Integer N : nodeCluster.keySet()) { // parallelize this.
            for (int i = 0; i < g.neighbor[N].length; i++) {
                if (nodeCluster.containsKey(g.neighbor[N][i])) {
                    ClusterNode u = nodeCluster.get(N);
                    ClusterNode v = nodeCluster.get(g.neighbor[N][i]);
                    u.neighbors.add(v);
                    v.neighbors.add(u);
                    checkNodes.add(u);
                    checkNodes.add(v);
                }
            }
        }

        for (ClusterNode u : checkNodes) {
            for (ClusterNode v : u.neighbors) {
                if (u.nodeNum < v.nodeNum) {
                    ClusterNode tmpClst = new ClusterNode();
                    tmpClst.left = u;
                    tmpClst.right = v;
                    tmpClst.parent = null;
                    tmpClst.size = 2;
                    computeSupport(tmpClst, false, false);
                    tmpClst.pathLocs.clear();
                    int sup = tmpClst.fwdSup + tmpClst.rcSup;

                    if (sup > tmpClst.left.bestNsup || (sup == tmpClst.left.bestNsup && tmpClst.right.bestNeighbor == tmpClst.left)) {
                        tmpClst.left.bestNsup = sup;
                        tmpClst.left.bestNeighbor = tmpClst.right;
                        ClusterEdge newE = new ClusterEdge(tmpClst.left, tmpClst.right, tmpClst.fwdSup + tmpClst.rcSup);
                        //tmpClst.left.edges.add(newE);
                        //tmpClst.right.edges.add(newE);
                        //edgeQ.add(newE);
                        edgeL.add(newE);
                    }
                }
            }
        }

        System.out.println("edgeL size: " + edgeL.size());

        ArrayList<ClusterEdge> edgeM = new ArrayList<ClusterEdge>();

        //IntStream.range(0, numGps).parallel().forEach(g -> {
        for (ClusterEdge E : edgeL) {
            if (E.u.bestNeighbor == E.v && E.v.bestNeighbor == E.u) { // merge
                finalizeEdge(E);
                edgeM.add(E);
            }
        }

        System.out.println("edgeM size: " + edgeM.size());

        for (ClusterEdge E : edgeM) {
            ClusterNode newClst = E.u.parent;
            newClst.neighbors = new TreeSet<ClusterNode>();
            for (ClusterNode n : newClst.left.neighbors) {
                if (n.parent == null) {
                    newClst.neighbors.add(n);
                    n.neighbors.remove(newClst.left);
                    n.neighbors.remove(newClst.right);
                    n.neighbors.add(newClst);
                } else {
                    newClst.neighbors.add(n.parent);
                }
            }
            for (ClusterNode n : newClst.right.neighbors) {
                if (n.parent == null) {
                    newClst.neighbors.add(n);
                    n.neighbors.remove(newClst.left);
                    n.neighbors.remove(newClst.right);
                    n.neighbors.add(newClst);
                } else {
                    newClst.neighbors.add(n.parent);
                }
            }
            newClst.neighbors.remove(newClst);
            checkNodes.addAll(newClst.neighbors);
            checkNodes.add(newClst);
        }

        System.out.println("checkNodes size: " + checkNodes.size());

//        System.out.println("edge queue size: " + edgeQ.size());
        ClusterEdge e;
        int count = 0;
//        while ((e = edgeQ.poll()) != null) {
//            if (e.potentialSup > 0 && e.u.parent == null && e.v.parent == null) {
//                finalizeEdge(e);
//                count++;
//                if (count % 1000 == 0) {
//                    System.out.println("# finalized: " + count);
//                }
//            }
//        }

        System.out.println(
                "edge list size: " + edgeL.size());
        edgeL.parallelStream()
                .forEach((E) -> {

                });

        // recheck initial edges
        System.out.println(
                "rechecking edges");
        for (Integer N
                : nodeCluster.keySet()) {
            for (int i = 0; i < g.neighbor[N].length; i++) {
                if (nodeCluster.containsKey(g.neighbor[N][i])) {
                    ClusterNode uroot = nodeCluster.get(N).findRoot();
                    ClusterNode vroot = nodeCluster.get(g.neighbor[N][i]).findRoot();
                    if (uroot != vroot) {
                        ClusterNode tmpClst = new ClusterNode();
                        tmpClst.left = uroot;
                        tmpClst.right = vroot;
                        tmpClst.parent = null;
                        tmpClst.size = uroot.size + vroot.size;
                        computeSupport(tmpClst, false, false);
                        tmpClst.pathLocs.clear();
                        if (tmpClst.fwdSup + tmpClst.rcSup > 0) {
                            ClusterEdge newE = new ClusterEdge(tmpClst.left, tmpClst.right, tmpClst.fwdSup + tmpClst.rcSup);
                            tmpClst.left.edges.add(newE);
                            tmpClst.right.edges.add(newE);
                            edgeQ.add(newE);
                        }
                    }
                }
            }
        }

        System.out.println(
                "recheck edge queue size: " + edgeQ.size());
        count = 0;
        while ((e = edgeQ.poll()) != null) {
            if (e.potentialSup > 0 && e.u.parent == null && e.v.parent == null) {
                finalizeEdge(e);
                count++;
            }
            if (count % 1000 == 0) {
                System.out.println("# finalized: " + count);
            }
        }

        System.out.println(
                "finding root FRs");
        HashSet<ClusterNode> roots = new HashSet<ClusterNode>();
        for (ClusterNode leaf
                : nodeCluster.values()) {
            roots.add(leaf.findRoot());
        }

        iFRQ = new PriorityQueue<ClusterNode>();

        System.out.println(
                "number of root FRs: " + roots.size());
        for (ClusterNode root : roots) {
            reportIFRs(root, 0);
        }

        System.out.println(
                "number of iFRs: " + iFRQ.size());
    }

    static void reportIFRs(ClusterNode clust, int parentSup) {
        if (clust.edges != null) {
            clust.edges.clear();
            clust.edges = null;
        }
        if ((clust.fwdSup + clust.rcSup) > parentSup
                && (clust.fwdSup + clust.rcSup) >= minSup
                && clust.fwdSup >= clust.rcSup) {
            computeSupport(clust, false, true);
            if (clust.avgLen >= minLen) {
                iFRQ.add(clust);
            }
        }
        if (clust.left != null) {
            reportIFRs(clust.left, Math.max(parentSup, clust.fwdSup + clust.rcSup));
        }
        if (clust.right != null) {
            reportIFRs(clust.right, Math.max(parentSup, clust.fwdSup + clust.rcSup));
        }
    }

    static long[] findFastaLoc(int path, int start, int stop) {
        long[] startStop = new long[2];
        long curStart = sequences.get(path).startPos;

        while (curStart > 0 && !startToNode.containsKey(curStart)) {
            curStart--;
        }

        int curIndex = 0;
        while (curIndex != start) {
            curStart += g.length[startToNode.get(curStart)] - (K - 1);
            curIndex++;
        }
        long offset = Math.max(0, sequences.get(path).startPos - curStart);
        startStop[0] = curStart - sequences.get(path).startPos + offset; // assume fasta seq indices start at 0
        while (curIndex != stop) {
            curStart += g.length[startToNode.get(curStart)] - (K - 1);
            curIndex++;
        }
        long seqLastPos = sequences.get(path).startPos + sequences.get(path).length - 1;
        startStop[1] = Math.min(seqLastPos, curStart + g.length[startToNode.get(curStart)] - 1)
                - sequences.get(path).startPos + 1; // last position is excluded in BED format
        return startStop;
    }

    int findLen(int[] pa) {
        int c = 0;
        for (int i = 0; i < pa.length; i++) {
            if (i == 0) {
                c += g.length[pa[i]];

            } else {
                c += g.length[pa[i]] - (K - 1);
            }
        }
        return c;
    }

    static void outputResults() {
        ClusterNode top;
        ArrayList<ClusterNode> iFRs = new ArrayList<ClusterNode>();
        while ((top = iFRQ.poll()) != null) {
            iFRs.add(top);
        }

        try {
            String paramString = "-k" + K + "-a" + alpha + "-kp" + kappa + "-sup" + minSup + "-mlen" + minLen;
            if (useRC) {
                paramString += "-rc";
            }
            String[] tmp = dotFile.split("/");
            String dotName = tmp[tmp.length - 1];
            tmp = fastaFile.split("/");
            String fastaName = tmp[tmp.length - 1];
            String filePrefix = dotName + "-" + fastaName;
            String rd = "results-" + filePrefix + "/";
            File resultsDir = new File(rd);
            resultsDir.mkdir();

            HashMap<Integer, TreeSet<Integer>> nodeFRset = new HashMap<Integer, TreeSet<Integer>>();
            BufferedWriter frOut = new BufferedWriter(new FileWriter(rd + filePrefix + paramString + ".frs.txt"));
            for (int fr = 0; fr < iFRs.size(); fr++) {
                ClusterNode iFR = iFRs.get(fr);
                String frName = "fr-" + fr;
                TreeSet<Integer> clustNodes = iFR.getNodeSet();
                frOut.write(frName);
                for (Integer n : clustNodes) {
                    frOut.write("," + n);
                    if (!nodeFRset.containsKey(n)) {
                        nodeFRset.put(n, new TreeSet<Integer>());
                    }
                    nodeFRset.get(n).add(fr);
                }
                frOut.write("\n");
            }
            frOut.close();

            System.out.println("writing bed file");
            BufferedWriter bedOut = new BufferedWriter(new FileWriter(rd + filePrefix + paramString + ".bed"));
            TreeMap<String, TreeMap<Integer, Integer>> seqFRcount = new TreeMap<String, TreeMap<Integer, Integer>>();
            TreeMap<String, TreeMap<Integer, LinkedList<String>>> seqIndxFRstr = new TreeMap<String, TreeMap<Integer, LinkedList<String>>>();
            int[] pathTotalSupLen = new int[paths.length];

            for (int fr = 0; fr < iFRs.size(); fr++) {
                ClusterNode iFR = iFRs.get(fr);
                if ((fr % 100) == 0) {
                    System.out.println("writing fr-" + fr);
                }
                ConcurrentLinkedQueue<PathSegment> supportingSegments = computeSupport(iFR, true, false);
                for (PathSegment ps : supportingSegments) {
                    String name = sequences.get(ps.path).label;
                    if (!seqFRcount.containsKey(name)) {
                        seqFRcount.put(name, new TreeMap<Integer, Integer>());
                    }
                    if (!seqFRcount.get(name).containsKey(fr)) {
                        seqFRcount.get(name).put(fr, 0);
                    }
                    seqFRcount.get(name).put(fr, seqFRcount.get(name).get(fr) + 1);
                    long[] startStop = findFastaLoc(ps.path, ps.start, ps.stop);
                    int frLen = (int) (startStop[1] - startStop[0]); // last position is excluded                                     
                    pathTotalSupLen[ps.path] += frLen;
                    bedOut.write(name // chrom
                            + "\t" + startStop[0] // chromStart (starts with 0)
                            + "\t" + startStop[1] // chromEnd
                            + "\t" + "fr-" + fr// name
                            + "\t" + Math.round(iFR.fwdSup + iFR.rcSup) // score
                            + "\t+" // strand
                            + "\t" + 0 // thickstart
                            + "\t" + 0 // thickend
                            + "\t" + colors[fr % colors.length] // itemRGB
                            + "\t" + frLen // FR length
                            + "\n");
                    if (!seqIndxFRstr.containsKey(name)) {
                        seqIndxFRstr.put(name, new TreeMap<Integer, LinkedList<String>>());
                    }
                    if (!seqIndxFRstr.get(name).containsKey(ps.start)) {
                        seqIndxFRstr.get(name).put(ps.start, new LinkedList<String>());
                    }
                    if (!seqIndxFRstr.get(name).containsKey(ps.stop)) {
                        seqIndxFRstr.get(name).put(ps.stop, new LinkedList<String>());
                    }
                    seqIndxFRstr.get(name).get(ps.start).addFirst(" [fr-" + fr + ":" + startStop[0]);
                    seqIndxFRstr.get(name).get(ps.stop).addLast(" fr-" + fr + ":" + startStop[1] + "] ");
                }
            }
            bedOut.close();

            System.out.println("writing dist file");
            BufferedWriter distOut = new BufferedWriter(new FileWriter(rd + filePrefix + paramString + ".dist.txt"));
            distOut.write("FR,size,support,avg length\n");
            for (int fr = 0; fr < iFRs.size(); fr++) {
                ClusterNode iFR = iFRs.get(fr);
                distOut.write("fr-" + fr + "," + iFR.size + "," + (iFR.fwdSup + iFR.rcSup) + "," + iFR.avgLen + "\n");
            }
            distOut.close();

            if (useRC) {
                System.out.println("writing rc file");
                BufferedWriter rcOut = new BufferedWriter(new FileWriter(rd + filePrefix + paramString + ".rc.txt"));
                for (int i = 0; i < paths.length / 2; i++) {
                    if (pathTotalSupLen[i + paths.length / 2] > pathTotalSupLen[i]) {
                        rcOut.write(sequences.get(i).label + "\n");
                    }
                }
                rcOut.close();
            }
            System.out.println("writing frpaths file");
            BufferedWriter frPathsOut = new BufferedWriter(new FileWriter(rd + filePrefix + paramString + ".frpaths.txt"));
            for (int i = 0; i < paths.length; i++) {
                String name = sequences.get(i).label;
                if (seqIndxFRstr.containsKey(name)) {
                    frPathsOut.write(name + ",");
                    for (int pos : seqIndxFRstr.get(name).keySet()) {
                        LinkedList<String> ll = seqIndxFRstr.get(name).get(pos);
                        for (String s : ll) {
                            frPathsOut.write(s);
                        }
                    }
                    frPathsOut.write("\n");
                }
            }
            frPathsOut.close();

            System.out.println("writing csfr file");
            BufferedWriter seqFROut = new BufferedWriter(new FileWriter(rd + filePrefix + paramString + ".csfr.txt"));
            for (String seq : seqFRcount.keySet()) {
                seqFROut.write(seq);
                for (Integer F : seqFRcount.get(seq).keySet()) {
                    seqFROut.write("," + F + ":" + seqFRcount.get(seq).get(F));
                }
                seqFROut.write("\n");
            }
            seqFROut.close();

            System.out.println("done");
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        }

    }

    public static void main(String[] args) {
        // parse args:
        if (args.length != 8) {
            System.out.println("Usage: java findFRs dotFile faFile K alpha kappa minsup minlen rcBool");
            System.out.println(Arrays.toString(args));
            System.exit(0);
        }
        dotFile = args[0];
        fastaFile = args[1];
        K = Integer.parseInt(args[2]);
        alpha = Double.parseDouble(args[3]);
        kappa = Integer.parseInt(args[4]);
        minSup = Integer.parseInt(args[5]);
        minLen = Integer.parseInt(args[6]);
        useRC = args[7].startsWith("T") || args[7].startsWith("t");

        readData();
        buildPaths();

        //startNode = new ClusterNode[g.numNodes];
        findFRs();
        outputResults();
    }
}
