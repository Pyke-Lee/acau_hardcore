package kr.pyke.acau_hardcore.data.randombox;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ClientBoxRegistry {
    private static final Map<String, BoxDefinition> BOXES = new LinkedHashMap<>();

    private ClientBoxRegistry() { }

    public static void setAll(List<BoxDefinition> definitions) {
        BOXES.clear();
        for (BoxDefinition def : definitions) {
            BOXES.put(def.id(), def);
        }
    }

    public static BoxDefinition get(String boxId) { return BOXES.get(boxId); }
    public static Collection<BoxDefinition> getAll() { return BOXES.values(); }
    public static boolean exists(String boxId) { return BOXES.containsKey(boxId); }
    public static void clear() { BOXES.clear(); }
}
