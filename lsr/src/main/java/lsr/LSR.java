package lsr;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import lsr.Client;
import lsr.Util;
import java.util.*;


public class LSR {

    private String username;
    private String password;
    private Client client;

    HashMap<String, String[]> topo;
    HashMap<String, String> names;

    HashMap<String, String[]> routingTable;

    public LSR(String username, String password) {
        this.username = username;
        this.password = password;
        client = new Client();
        this.topo = Util.readTopoConfig();
        this.names = Util.readnamesConfig();
        this.routingTable = new HashMap<>();
        this.routingTable.put(username, getNeighbors(username));
    }

    /**
     * Obtener el identificador del usuario
     *
     * @param user
     * @return
     */
    public String getUserIdentifier(String user) {
        Set<String> keys = names.keySet();
        for (String key : keys) {
            if (names.get(key).equals(user)) {
                return key;
            }
        }
        return null;
    }

    /**
     * Obtener vecinos de un usuario
     *
     * @param username
     * @return
     */
    public String[] getNeighbors(String username) {
        String identifier = getUserIdentifier(username);
        String[] neighbors = topo.get(identifier);

        ArrayList<String> emails = new ArrayList<>();

        for (String neighbor : neighbors) {
            emails.add(names.get(neighbor));
        }
        return emails.toArray(new String[emails.size()]);
    }

    /**
     * Este metodo envia un hello world a los neighbors con los vecinos y estado
     */
    public void sendInitialLSA() {
        JSONObject json = new JSONObject();
        json.put("type", "LSA");
        json.put("from", this.username);

        JSONObject jsonObject = new JSONObject();

        for (String key : routingTable.keySet()) {
            String[] values = routingTable.get(key);
            JSONArray neighborsJSON = new JSONArray();
            for (String value : values) {
                neighborsJSON.add(value);
            }

            jsonObject.put(key, neighborsJSON);
        }

        json.put("neighbors", jsonObject);


        String[] neighbors = routingTable.get(username);
        System.out.println("Initial LSR: " + Arrays.toString(neighbors));

        for (String neighbor : neighbors) {
            try {
                Chat chat = client.getConnection().getChatManager().createChat(neighbor, null);
                json.put("to", neighbor);
                chat.sendMessage(json.toJSONString());
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * Este metodo prepara el routing table para ser posteriormente enviada en formato json
     * @return
     */
    private JSONObject getRoutingTable(){
        JSONObject routingJSON = new JSONObject();

        for (String key : routingTable.keySet()) {
            String[] values = routingTable.get(key);
            JSONArray neighborsJSON = new JSONArray();
            for (String value : values) {
                neighborsJSON.add(value);
            }
            routingJSON.put(key, neighborsJSON);
        }

        return routingJSON;
    }

    /**
     * Metodo que prepara nuestro mensaje que contiene el routing table para compartirla con los vecinos.
     */
    public void sendPeriodicLSA() {
        JSONObject json = new JSONObject();
        json.put("type", "LSAACTU");
        json.put("from", this.username);
        json.put("neighbors", getRoutingTable());


        String[] neighbors = routingTable.get(username);

        for (String neighbor : neighbors) {
            try {
                Chat chat = client.getConnection().getChatManager().createChat(neighbor, null);
                json.put("to", neighbor);
                chat.sendMessage(json.toJSONString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Configurar un intervalo para poder mandar configuracion a los demas nodos cada 10 segundos
     */
    private void configuraLSATimer() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                sendPeriodicLSA();
            }
        }, 0, 10000);
    }

    /**
     * Metodo para actualizar nuestra routing table
     * @param object
     */
    private void updateRoutingtable(JSONObject object) {
        JSONObject neighbors = (JSONObject) object.get("neighbors");
        Set<String> keys = neighbors.keySet();

        for (String key : keys) {
            JSONArray neighborsJSON = (JSONArray) neighbors.get(key);
            String[] neighborsArray = new String[neighborsJSON.size()];
            for (int i = 0; i < neighborsJSON.size(); i++) {
                neighborsArray[i] = (String) neighborsJSON.get(i);
            }

            routingTable.put(key, neighborsArray);
        }
    }

    /**
     * Este metodo se encarga de actualizar la tabla de enrutamiento cuando un nodo al que nos conectamos nos
     * responde con su routing table
     * @param message
     */
    private void handleLSARENV(JSONObject message) {
        updateRoutingtable(message);
    }

    /**
     * metodo que actualiza la tabla de enrutamiento
     * @param message
     */
    private void handleLSAACTU(JSONObject message) {
        // Actualizar mi routing table
        updateRoutingtable(message);
    }

    /**
     * Metodo para tratar los mensajes de configuracion de otro nodo que inicio la conexion y
     * transmitir esta informacion a los demas nodos vecinos
     * @param message
     */
    private void handleLSA(JSONObject message) {
        String[] neighbors = routingTable.get(username);
        String fromLSA = (String) message.get("from");
        String toLSA = (String) message.get("to");
        neighbors = Arrays.stream(neighbors).filter(neighbor -> !neighbor.equals(fromLSA) || !neighbor.equals(toLSA)).toArray(String[]::new);


        // Actualizar mi routing table
        updateRoutingtable(message);

        //  Mandar LSA del nodo a los vecinos de este nodo omitiendo el que lo envio
        JSONObject newMessage = new JSONObject();
        newMessage.put("type", "LSAACTU");
        newMessage.put("from", this.username);
        newMessage.put("neighbors", getRoutingTable());

        for (String neighbor : neighbors) {
            try {
                Chat chat = client.getConnection().getChatManager().createChat(neighbor, null);
                message.put("to", neighbor);
                chat.sendMessage(newMessage.toJSONString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Mandarle al nodo que envio la LSA el LSA de este nodo
        JSONObject json = new JSONObject();
        json.put("type", "LSARENV");
        json.put("neighbors", getRoutingTable());

        try {
            Chat chat = client.getConnection().getChatManager().createChat(fromLSA, null);
            json.put("from", username);
            json.put("to", fromLSA);
            chat.sendMessage(json.toJSONString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Metodo para reenviar mensaje o mostrar el mensaje si es para el nodo
     * @param message
     */
    private void handleMessage(JSONObject message) {
        JSONArray path = (JSONArray) message.get("path");
        String finalUser = (String) path.get(path.size() - 1);

        if (username.equals(finalUser)) {
            System.out.println("Mensaje recibido: " + message.toJSONString());
        } else {
            int currentIndex = path.indexOf(username);
            String nextUser = (String) path.get(currentIndex + 1);

            try {
                Chat chat = client.getConnection().getChatManager().createChat(nextUser, null);
                message.put("to", nextUser);
                chat.sendMessage(message.toJSONString());
            } catch (Exception e) {
                e.printStackTrace();
            }

            System.out.println("Mensaje reenviado a: " + nextUser);
        }
    }

    /**
     * Metodo para conectar con el servidor de alumchat.fun
     */
    public void connect() {
        client.login(username, password);

        // Set listener para LSA information
        client.getConnection().addPacketListener((packet) -> {
            try {
                if (packet instanceof Message) {
                    Message message = (Message) packet;

                    if (message.getBody() != null) {
                        JSONObject jsonObject = (JSONObject) new JSONParser().parse(message.getBody());

                        String from = (String) jsonObject.get("from");
                        String to = (String) jsonObject.get("to");
                        String type = (String) jsonObject.get("type");


                        if (from.equals(username) || !to.equals(username)) {
                            return;
                        }


                        if (type.equals("LSA")) {
                            handleLSA(jsonObject);
                        }else if (type.equals("LSARENV")) {
                            handleLSARENV(jsonObject);
                        } else if (type.equals("LSAACTU")) {
                            handleLSAACTU(jsonObject);
                        } else if (type.equals("MESSAGE")) {
                            handleMessage(jsonObject);
                        }
                    }
                }
            } catch (Exception e) {}

        }, new PacketTypeFilter(Packet.class));

        // Notificar a los vecinos del estado del nodo
        this.sendInitialLSA();

        // Configurar timer para enviar LSA a neighbors
        this.configuraLSATimer();
    }

    /**
     * Metodo para encontrar la ruta mas corta a un nodo tomando en cuenta el routing table
     * @param to
     * @return
     */
    private String[] getShortestPath(String to) {
        try {

            // Crear matriz de adyacencia
            int[][] adjacencyMatrix = new int[routingTable.size()][routingTable.size()];
            HashMap<String, Integer> indexes = new HashMap<>();

            Set<String> keysRouting = routingTable.keySet();

            int i = 0;
            for (String key : keysRouting) {
                indexes.put(key, i);
                i++;
            }

            for (String key : keysRouting) {
                String[] values = routingTable.get(key);
                for (String value : values) {
                    if (indexes.containsKey(value)) {
                        adjacencyMatrix[indexes.get(key)][indexes.get(value)] = 1;
                    }
                }
            }

            int senderIndex = indexes.get(username);
            int toIndex = indexes.get(to);

            // Dijkstra
            int[] dist = new int[adjacencyMatrix.length];
            boolean[] visited = new boolean[adjacencyMatrix.length];
            int[] prev = new int[adjacencyMatrix.length];
            for (int j = 0; j < adjacencyMatrix.length; j++) {
                dist[j] = Integer.MAX_VALUE;
                visited[j] = false;
                prev[j] = -1;
            }
            dist[senderIndex] = 0;

            for (int j = 0; j < adjacencyMatrix.length; j++) {
                int min = Integer.MAX_VALUE;
                int minIndex = -1;
                for (int k = 0; k < adjacencyMatrix.length; k++) {
                    if (!visited[k] && dist[k] < min) {
                        min = dist[k];
                        minIndex = k;
                    }
                }
                visited[minIndex] = true;
                for (int k = 0; k < adjacencyMatrix.length; k++) {
                    if (!visited[k] && adjacencyMatrix[minIndex][k] != 0 && dist[minIndex] != Integer.MAX_VALUE && dist[minIndex] + adjacencyMatrix[minIndex][k] < dist[k]) {
                        dist[k] = dist[minIndex] + adjacencyMatrix[minIndex][k];
                        prev[k] = minIndex;
                    }
                }
            }

            // Recorrer el camino mas corto
            ArrayList<String> path = new ArrayList<>();
            int current = toIndex;
            while (current != -1) {
                path.add(keysRouting.toArray(new String[0])[current]);
                current = prev[current];
            }
            Collections.reverse(path);

            if (path.size() > 0) {
                return path.toArray(new String[0]);
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Metodo para enviar un mensaje a un usuario
     * @param to
     * @param message
     */
    private void sendMessage(String to, String message) {
        // Serach the shortest path to the destination
        String[] path = this.getShortestPath(to);

        if (path == null) {
            System.out.println("Could not find a way to deliver the message");
            return;
        } else {
            System.out.println("Found path: " + Arrays.toString(path));
            JSONObject json = new JSONObject();
            /*json.put("type", "MESSAGE");*/
            json.put("message", message);
            json.put("from", username);
            json.put("nodes", path.length - 1);
            json.put("distance", path.length - 2);

            JSONArray pathJSON = new JSONArray();

            for (String key : path) {
                pathJSON.add(key);
            }

            json.put("Nodos", pathJSON);


            try {
                Chat chat = client.getConnection().getChatManager().createChat(path[1], null);
                json.put("to", path[1]);
                chat.sendMessage(json.toJSONString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("Message send to: " + path[1]);
        }
    }

    /**
     * Main
     * @param args
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Type username: ");
        String username = scanner.nextLine();
        System.out.print("Type Password: ");
        String password = scanner.nextLine();

        LSR routing = new LSR(username, password);
        routing.connect();

        while (true) {
            System.out.print("Enter Recipient Username: ");
            String to = scanner.nextLine();
            System.out.print("Type Message: ");
            String message = scanner.nextLine();
            try {
                routing.sendMessage(to, message);
            } catch (Exception e) {
                System.out.println("Error sending message");
                e.printStackTrace();
            }
        }
    }
}
