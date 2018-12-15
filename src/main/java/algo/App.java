package algo;

import static algo.Dijkstra.calculatePathAsAMap;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.*;  //todo: Remove *
import java.util.stream.Collectors;

import org.apache.log4j.Logger;


public class App implements Serializable {

    private final static Logger logger = Logger.getLogger(App.class);

    private static final String SEPARATOR = ";";
    private static final String CSV_FILE = "src/main/resources/distance.csv";
    private static final String path = "src/main/resources/serialized.data";
    private static final String INITIAL_POINT = "Riga";
    private static final String DESTINATION_POINT = "Tallinn";

    public static void main(String[] args) throws IOException {

        List<List<String>> links = new ArrayList<>();
        ;
        Map<String, Node> nodesMap = new HashMap<>();
        Map<String, Node> newNodeMap = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(CSV_FILE)))) {

            String line = reader.readLine();
            logger.info("Reading file, creating list of cities, added");

            while (line != null) {
                String[] str = line.split(SEPARATOR);

                nodesMap.put(str[0], new Node(str[0]));
                nodesMap.put(str[1], new Node(str[1]));
                links.add(Arrays.asList(str));
                line = reader.readLine();
            }
        } catch (IOException ioex) {
            logger.error("Problem with file reading: " + ioex.getMessage());
        }

        logger.info("Starting serialization of a map");

        FileOutputStream fos = new FileOutputStream(path);
        ObjectOutputStream out = new ObjectOutputStream(fos);
        out.writeObject(nodesMap);
        logger.info("Serialization is finished");

        logger.info("Starting deserialization of a map");

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path))) {

            newNodeMap = (Map<String, Node>) ois.readObject();

        } catch (ClassNotFoundException | IOException ex) {
            logger.error("Message: " + ex.getMessage());
        }

        nodesMap = newNodeMap;

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

        for (Map.Entry<String, Node> n : nodesMap.entrySet()) {
            graphNew.addNode(n.getValue());
        }
        logger.info("Graph is ready");


        logger.info("Calculating a shortest path");
        Map<String, Node> resultMap = calculatePathAsAMap(graphNew, nodesMap.get(INITIAL_POINT));
        Node result = resultMap.get(DESTINATION_POINT);

        logger.info("Generating a list of nodes of shortest path");
        List<String> geoPoints = result.getShortestPath().stream().map(element -> element.getName() + " [" + Math.round(element.getDistance()) + "]")
                .collect(Collectors.toList());

        logger.info("Printing a list of nodes one by one");
        StringBuilder pathAsString = new StringBuilder();

        for (String geoPoint : geoPoints) {
            pathAsString.append(geoPoint).append(" -> ");
        }
        pathAsString.append(result.getName()).append(" [").append(Math.round(result.getDistance())).append("]");

        logger.info("The shortest path is: " + pathAsString.toString());
    }
}