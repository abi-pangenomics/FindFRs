/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package findFRs;

import java.io.*;
import java.util.*;
import org.apache.commons.io.*;

/**
 *
 * @author bmumey
 */
public class ReadInput {

    public static Graph readDotFile(String fileName) {

        Graph g = new Graph();
        TreeMap<Integer, TreeSet<Integer>> nodeNeighbors = new TreeMap<Integer, TreeSet<Integer>>();
        TreeMap<Integer, ArrayList<Long>> nodeStarts = new TreeMap<Integer, ArrayList<Long>>();
        TreeMap<Integer, Integer> nodeLength = new TreeMap<Integer, Integer>();
        g.maxStart = 0;
        int minLen = Integer.MAX_VALUE;
        System.out.println("reading dot file: " + fileName);

        FileInputStream inputStream = null;
        Scanner sc = null;
        try {
            inputStream = new FileInputStream(fileName);
            sc = new Scanner(inputStream, "UTF-8");
            //LineIterator it = FileUtils.lineIterator(new File(fileName), "UTF-8");
            //BufferedReader br = new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(fileName))));
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                Scanner lineScanner;
                if (line.contains("label")) { //node
                    lineScanner = new Scanner(line);
                    lineScanner.useDelimiter(" ");
                    lineScanner.next();
                    int node = Integer.parseInt(lineScanner.next().trim());
                    nodeNeighbors.put(node, new TreeSet<Integer>());
                    String label = lineScanner.next();
                    label = label.split("\"")[1];
                    String[] l = label.split(":");
                    String[] starts = l[0].split(",");
                    nodeStarts.put(node, new ArrayList<Long>());
                    for (String s : starts) {
                        long start = Long.parseLong(s);
                        nodeStarts.get(node).add(start);
                        g.maxStart = Math.max(g.maxStart, start);
                    }
                    int nodeLen = Integer.parseInt(l[1]);
                    minLen = Math.min(minLen, nodeLen);
                    nodeLength.put(node, Integer.parseInt(l[1]));
                    if (node % 50000 == 0) {
                        System.out.println("reading node: " + node);
                    }
                }
                if (line.contains("->")) { //edge
                    lineScanner = new Scanner(line);
                    lineScanner.useDelimiter("->");
                    int tail = Integer.parseInt(lineScanner.next().trim());
                    int head = Integer.parseInt(lineScanner.next().trim());
                    nodeNeighbors.get(tail).add(head);
                    //System.out.println("edge: " + tail + "->" + head);
                }
            }
            if (inputStream != null) {
                inputStream.close();
            }
            if (sc != null) {
                sc.close();
            }
        } catch (Exception ex) {
            System.err.println(ex);
            ex.printStackTrace();
            System.exit(-1);
        }

        System.out.println("K = " + minLen);
        FindFRs.K = minLen;
        g.numNodes = nodeNeighbors.keySet().size();
        System.out.println("number of nodes: " + g.numNodes);
        g.neighbor = new int[g.numNodes][];
        for (int i = 0; i < g.neighbor.length; i++) {
            g.neighbor[i] = new int[nodeNeighbors.get(i).size()];
            int j = 0;
            for (Integer jobj : nodeNeighbors.get(i)) {
                g.neighbor[i][j++] = jobj;
            }
        }
        g.starts = new long[g.numNodes][];
        for (int i = 0; i < g.neighbor.length; i++) {
            g.starts[i] = new long[nodeStarts.get(i).size()];
            int j = 0;
            for (Long jobj : nodeStarts.get(i)) {
                g.starts[i][j++] = jobj;
            }
        }
        g.length = new int[g.numNodes];
        for (int i = 0; i < g.neighbor.length; i++) {
            g.length[i] = nodeLength.get(i);
        }

        return g;
    }

    public static ArrayList<Sequence> readFastaFile(String fileName) {
        int numSeqRead = 0;
        ArrayList<Sequence> sequences = new ArrayList<Sequence>();
        System.out.println("reading fasta file: " + fileName);
        readSequenceFromFile(fileName);
        for (int i = 0; i < description.length; i++) {
            Sequence s = new Sequence();
            s.label = description[i].replace(',', ';'); //description[i].split(" ")[0];
            s.seq = sequence[i];
            sequences.add(s);
        }

        return sequences;

//        try {
//            BufferedReader br = new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(fileName))));
//            //BufferedReader br = new BufferedReader(new FileReader(fileName));
//            String line;
//            while ((line = br.readLine()) != null) {
//                if (line.contains(">")) { // new sequence
//                    String label = line.substring(1, line.length());
//                    Sequence nextSeq = new Sequence();
//                    nextSeq.label = label.split(" ")[0]; // get what is left of first space
//                    nextSeq.seq = "";
//                    sequences.add(nextSeq);
//                    numSeqRead++;
//                    if (numSeqRead % 100 == 0) {
//                        System.out.println("read seq: " + numSeqRead);
//                    }
//                } else { // sequence
//                    Sequence lastSeq = sequences.get(sequences.size() - 1);
//                    lastSeq.seq += line.toUpperCase();
//                }
//            }
//        } catch (Exception ex) {
//            System.err.println(ex);
//            ex.printStackTrace();
//            System.exit(-1);
//        }
    }

    private static String[] description;
    private static String[] sequence;

    static void readSequenceFromFile(String fileName) {
        List desc = new ArrayList();
        List seq = new ArrayList();
        try {
            LineIterator it = FileUtils.lineIterator(new File(fileName), "UTF-8");
            //BufferedReader in = new BufferedReader(new FileReader(file));
            StringBuffer buffer = new StringBuffer();

            if (!it.hasNext()) {
                throw new IOException(fileName + " is an empty file");
            }
            String line = it.nextLine();
            if (line.charAt(0) != '>') {
                throw new IOException("First line of " + fileName + " should start with '>'");
            } else {
                desc.add(line.substring(1));
            }
            while (it.hasNext()) {
                line = it.nextLine();
                //for (line = it.nextLine().trim(); line != null; line = it.nextLine()) {
                if (line.length() > 0 && line.charAt(0) == '>') {
                    seq.add(buffer.toString());
                    buffer = new StringBuffer();
                    desc.add(line.substring(1));
                } else {
                    buffer.append(line.trim().toUpperCase());
                }
            }
            if (buffer.length() != 0) {
                seq.add(buffer.toString());
            }
        } catch (IOException e) {
            System.out.println("Error when reading " + fileName);
            e.printStackTrace();
        }

        description = new String[desc.size()];
        sequence = new String[seq.size()];
        for (int i = 0; i < seq.size(); i++) {
            description[i] = (String) desc.get(i);
            sequence[i] = (String) seq.get(i);
        }

    }

//    //return first sequence as a String
//    public String getSequence() {
//        return sequence[0];
//    }
//
//    //return first xdescription as String
//    public String getDescription() {
//        return description[0];
//    }
//
//    //return sequence as a String
//    public String getSequence(int i) {
//        return sequence[i];
//    }
//
//    //return description as String
//    public String getDescription(int i) {
//        return description[i];
//    }
//
//    public int size() {
//        return sequence.length;
//    }
}
