package pt.ulisboa.tecnico.cmu.fragment.dummy;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class with the content of the Monument list on MainActivity
 */
public class MonumentsListContent {
    /**
     * An array of monuments items.
     */
    public static List<MonumentItem> ITEMS = new ArrayList<>();

    /**
     * A map of monuments items, by ID.
     */
    public static final Map<String, MonumentItem> ITEM_MAP = new HashMap<>();


    public static void addMonuments(List<String> monumentsNames){
        // Add some sample items.
        for (int i = 1; i <= monumentsNames.size(); i++) {
            addItem(createDummyItem(i,monumentsNames.get(i-1)));
        }
    }


    private static void addItem(MonumentItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private static MonumentItem createDummyItem(int position, String content) {
        return new MonumentItem(String.valueOf(position), content);
    }

    /**
     * A monument item representing a monument
     */
    public static class MonumentItem {
        public final String id;
        public final String content;

        public MonumentItem(String id, String content) {
            this.id = id;
            this.content = content;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
