/*
 * @Author Christopher Marra, Carter Page
 * This class is a modified version of the free to use class which sorts a map array
 * from http://stackoverflow.com/questions/109383/how-to-sort-a-mapkey-value-on-the-values-in-java
 */

package util;

import java.util.*;

public class MapUtil
{
    public static <K, V extends Comparable<? super V>> Map<K, V>
            sortByValueLargest(Map<K, V> map)
    {
        List<Map.Entry<K, V>> list =
                new LinkedList<Map.Entry<K, V>>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>()
        {
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2)
            {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });
        

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list)
        {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public static <K, V extends Comparable<? super V>> Map<K, V>
            sortByValueSmallest(Map<K, V> map)
    {
        List<Map.Entry<K, V>> list =
                new LinkedList<Map.Entry<K, V>>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>()
        {
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2)
            {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list)
        {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
}
