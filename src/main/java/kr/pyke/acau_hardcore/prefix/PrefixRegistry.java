package kr.pyke.acau_hardcore.prefix;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PrefixRegistry {
    private static final Map<String, PrefixData> PREFIXES = new HashMap<>();

    public static void register(String id, PrefixData data) { PREFIXES.put(id, data); }

    public static PrefixData get(String id) { return PREFIXES.get(id); }

    public static Collection<PrefixData> getAll() { return Collections.unmodifiableCollection(PREFIXES.values()); }

    public static Collection<String> getKeys() { return Collections.unmodifiableCollection(PREFIXES.keySet()); }

    public static void clear() { PREFIXES.clear(); }
}
