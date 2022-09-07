package lsr;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.FileReader;
import java.util.HashMap;

public class Util {

    public static HashMap<String, String[]> readTopoConfig() {
        try {
            JSONObject jsonObject = readFile("lsr/src/main/resources/topo.json");

            if (jsonObject == null) {
                System.out.println("File not found");
                return null;
            }
            HashMap<String, String[]> topo = new HashMap<>();
            for (Object key : jsonObject.keySet()) {
                String topoKey = (String) key;
                JSONArray topoValues = (JSONArray) jsonObject.get(topoKey);
                String[] topoValueArray = (String[]) topoValues.toArray(new String[topoValues.size()]);
                topo.put(topoKey, topoValueArray);
            }
            return topo;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static HashMap<String, String> readnamesConfig() {
        try {
            JSONObject jsonObject = readFile("lsr/src/main/resources/names.json");

            if (jsonObject == null) {
                System.out.println("File not found");
                return null;
            }
            HashMap<String, String> topo = new HashMap<>();

            for (Object key : jsonObject.keySet()) {
                String topoKey = (String) key;
                String topoValue = (String) jsonObject.get(topoKey);
                topo.put(topoKey, topoValue);
            }
            return topo;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static JSONObject readFile(String pathname) {
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new FileReader(pathname));
            JSONObject jsonObject = (JSONObject) obj;
            return jsonObject;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}