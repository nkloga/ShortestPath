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


        while (line != null) {
            String[] str = line.split(SEPARATOR);
            nodesMap.put(str[0], new Node(str[0]));
            nodesMap.put(str[1], new Node(str[1]));
            links.add(Arrays.asList(str));
            logger.info("reading file, creating list of cities, added: " + str[0] + " and " + str[1]);
            line = reader.readLine();
        }
        fis.close();
        reader.close();

        String path = "/Users/nk/IdeaProjects/Shortpath/src/main/java/algo/serialized.data";
        FileOutputStream fos = new FileOutputStream(path);
        ObjectOutputStream out = new ObjectOutputStream(fos);
        out.writeObject(nodesMap);

        FileInputStream fis2 = new FileInputStream(path);
        ObjectInputStream ois = new ObjectInputStream(fis2);

        Map<String, Node> newNodeMap;
        try {
            newNodeMap = (Map<String, Node>) ois.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < links.size(); i++) {
            Node initialNode = nodesMap.get(links.get(i).get(0));
            Node finalNode = nodesMap.get(links.get(i).get(1));
            Double weight = Double.parseDouble(links.get(i).get(2));

            initialNode.addDestination(finalNode, weight);
            nodesMap.put(initialNode.getName(), initialNode);
            finalNode.addDestination(initialNode, weight);
            nodesMap.put(finalNode.getName(), finalNode);
        }
        Graph graphNew = new Graph();
        for (Map.Entry n : nodesMap.entrySet()) {
            graphNew.addNode((Node) n.getValue());
        }

        String initialPoint = "Riga";
        String destinationPoint = "Tallinn";

        Map<String, Node> resultMap = calculatePathAsAMap(graphNew, nodesMap.get(initialPoint));
        Node result = resultMap.get(destinationPoint);

        List<String> geoPoints = result.getShortestPath().stream().map(element -> element.getName() + " [" + Math.round(element.getDistance()) + "]")
                .collect(Collectors.toList());

        StringBuilder pathAsString = new StringBuilder();
        for (String geoPoint : geoPoints) {
            pathAsString.append(geoPoint).append(" -> ");
        }
        pathAsString.append(result.getName()).append(" [").append(Math.round(result.getDistance())).append("]");
        logger.info(pathAsString.toString());
    }
}
