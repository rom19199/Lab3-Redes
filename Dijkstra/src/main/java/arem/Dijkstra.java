package arem;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;

public class Dijkstra<T> {

    public void calculateShortestPath(Node<T> source) {
        source.setDistance(0);
        Set<Node<T>> settledNodes = new HashSet<>();
        Queue<Node<T>> unsettledNodes = new PriorityQueue<>(Collections.singleton(source));
        while (!unsettledNodes.isEmpty()) {
            Node<T> currentNode = unsettledNodes.poll();
            currentNode.getAdjacentNodes()
                    .entrySet().stream()
                    .filter(entry -> !settledNodes.contains(entry.getKey()))
                    .forEach(entry -> {
                        evaluateDistanceAndPath(entry.getKey(), entry.getValue(), currentNode);
                        unsettledNodes.add(entry.getKey());
                    });
            settledNodes.add(currentNode);
        }
    }

    private void evaluateDistanceAndPath(Node<T> adjacentNode, Integer edgeWeight, Node<T> sourceNode) {
        Integer newDistance = sourceNode.getDistance() + edgeWeight;
        if (newDistance < adjacentNode.getDistance()) {
            adjacentNode.setDistance(newDistance);
            adjacentNode.setShortestPath(Stream.concat(sourceNode.getShortestPath().stream(), Stream.of(sourceNode)).toList());
        }
    }

    public void printPaths(List<Node<T>> nodes) {
        System.out.println("---------Tabla de rutas del nodo fuente---------");;
        String[] lista = new String[nodes.size()];
        final Integer[] id = new Integer[1];
        id[0] = 1;
        nodes.forEach(node -> {
            String path = node.getShortestPath().stream()
                    .map(Node::getName).map(Objects::toString)
                    .collect(Collectors.joining(" -> "));

            String ultiPath = (path.isBlank() ? "    " : " " + id[0] + ". ") + (path.isBlank()
            ? "%s : %s".formatted(node.getName(), node.getDistance())
            : "%s -> %s : %s".formatted(path, node.getName(), node.getDistance()));
            
            if (path.isBlank()) lista[0] = ultiPath;
            else {
                lista[id[0]] = ultiPath;
                id[0] += 1;
            }
        });
        System.out.println("");
        for (String element : lista) {
            System.out.println(element);
        }
        System.out.println("");
    }

    public void sendMessage(int s ,int d , Node<T>[] nodes, String msg, ChatManager chatManager){
        List<String> pathTo = nodes[d].getShortestPath().stream()
                    .map(Node::getName).map(Objects::toString)
                    .collect(Collectors.toList());

        final String[] sendNode = new String[1];
        final int[] id = new int[1];
        final EntityBareJid[] jid = new EntityBareJid[1];
        sendNode[0] = null;
        id[0] = 0;
        jid[0] = null;
        String node, nextNode = null;
        for(int i = 0; i < pathTo.size(); i++){
            node = pathTo.get(i);
            if (i + 1 < pathTo.size()) nextNode = pathTo.get(i + 1);
            else nextNode = nodes[d].getName().toString();

            if (!node.equals(nodes[s].getName())){
                int idNode = (int)node.charAt(0) - 65;
                String path = nodes[idNode].getShortestPath().stream()
                        .map(Node::getName).map(Objects::toString)
                        .collect(Collectors.joining(" -> "));
    
                String ultiPath = "%s -> %s".formatted(path, nodes[idNode].getName());

                String message = setMessage(s, d, msg, nodes, sendNode[0], ultiPath, id[0], nodes[idNode].getDistance(), nextNode);

                //Mensaje enviado al nodo
                try{
                    jid[0] = JidCreate.entityBareFrom(nodes[idNode].getEmail());
                    Chat chat = chatManager.chatWith(jid[0]);
                    chat.send(message);
                }catch(Exception e){
                    e.printStackTrace();
                }

            }
            id[0] += 1;
            sendNode[0] = node;
        }

        nextNode = null;

        String path = nodes[d].getShortestPath().stream()
                    .map(Node::getName).map(Objects::toString)
                    .collect(Collectors.joining(" -> "));

        String ultiPath = "%s -> %s".formatted(path, nodes[d].getName());

        try{
            String message = setMessage(s, d, msg, nodes, sendNode[0], ultiPath, id[0], nodes[d].getDistance(), nextNode);
            jid[0] = JidCreate.entityBareFrom(nodes[d].getEmail());
            Chat chat = chatManager.chatWith(jid[0]);
            chat.send(message);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public String setMessage(int s, int d, String msg, Node<T>[] nodes, String node, String path, int saltos, int distance, String nextNode){
        String message = "\n\n" + "----------------------Mensaje----------------------" + "\n" + 
                         "Nodo fuente: " + nodes[s].getName() + " \n" +
                         "Enviado del nodo: " + node + "\n" +
                         "Nodo destino: " + nodes[d].getName() + " \n" +
                         "Saltos dados desde nodo inicial:" + saltos + "\n" +
                         "Distancia: " + distance + "\n" + 
                         "Listado de nodos: " + path +  
                         (nextNode == null ? "\n" : "\n" + "Nodo a enviar -> " + nextNode + "\n") +
                         "Mensaje: " + msg + "\n";
 
        return message;
    }

}
