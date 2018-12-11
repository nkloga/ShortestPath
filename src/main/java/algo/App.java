package algo;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static algo.Dijkstra.calculatePathAsAMap;

public class App implements Serializable {

    final static Logger logger = Logger.getLogger(App.class);

    public static void main(String[] args) throws IOException {

        final String SEPARATOR = ";";

        String csvFile = "/Users/nk/IdeaProjects/Shortpath/src/main/java/algo/distance.csv";
        FileInputStream fis = new FileInputStream(csvFile);
        BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
        String line = reader.readLine();
        List<List<String>> links = new ArrayList<>();
        Map<String, Node> nodesMap = new HashMap<>();

        logger.info("Reading file, creating list of cities, added");

        while (line != null) {
            String[] str = line.split(SEPARATOR);
            nodesMap.put(str[0], new Node(str[0]));
            nodesMap.put(str[1], new Node(str[1]));
            links.add(Arrays.asList(str));
            line = reader.readLine();
        }
        fis.close();
        reader.close();
        logger.info("Starting serialization of a map");
        String path = "/Users/nk/IdeaProjects/Shortpath/src/main/java/algo/serialized.data";
        FileOutputStream fos = new FileOutputStream(path);
        ObjectOutputStream out = new ObjectOutputStream(fos);
        out.writeObject(nodesMap);
        logger.info("Serialization is finished");

        FileInputStream fis2 = new FileInputStream(path);
        ObjectInputStream ois = new ObjectInputStream(fis2);

        logger.info("Starting deserialization of a map");

        Map<String, Node> newNodeMap;
        try {
            newNodeMap = (Map<String, Node>) ois.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        logger.info("Deserialization is finished");

        logger.info("Creating a map of routes");
        for (int i = 0; i < links.size(); i++) {
            Node initialNode = nodesMap.get(links.get(i).get(0));
            Node finalNode = nodesMap.get(links.get(i).get(1));
            Double weight = Double.parseDouble(links.get(i).get(2));

            initialNode.addDestination(finalNode, weight);
            nodesMap.put(initialNode.getName(), initialNode);
            finalNode.addDestination(initialNode, weight);
            nodesMap.put(finalNode.getName(), finalNode);
        }
        logger.info("Preparing a graph, filling it with nodes");
        Graph graphNew = new Graph();
        for (Map.Entry n : nodesMap.entrySet()) {
            graphNew.addNode((Node) n.getValue());
        }
        logger.info("Graph is ready");

        String initialPoint = "Riga";
        String destinationPoint = "Tallinn";
        logger.info("Calculating a shortest path");
        Map<String, Node> resultMap = calculatePathAsAMap(graphNew, nodesMap.get(initialPoint));
        Node result = resultMap.get(destinationPoint);
        logger.info("Generating a list of nodes of shortest path");
        List<String> geoPoints = result.getShortestPath().stream().map(element -> element.getName() + " [" + Math.round(element.getDistance()) + "]")
                .collect(Collectors.toList());

        StringBuilder pathAsString = new StringBuilder();
        logger.info("Printing a list of nodes one by one");
        for (String geoPoint : geoPoints) {
            pathAsString.append(geoPoint).append(" -> ");
        }
        pathAsString.append(result.getName()).append(" [").append(Math.round(result.getDistance())).append("]");
        logger.info("The shortest path is: " + pathAsString.toString());
    }
}
