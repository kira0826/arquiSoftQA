package utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CounterManger {
    


    private Map<String, Integer> requestCounts;


    public CounterManger() {
        this.requestCounts = new ConcurrentHashMap();
    }

    public void increment(String host) {
        requestCounts.merge(host, 1, Integer::sum);
    }

    public int getCount(String host) {
        return requestCounts.getOrDefault(host, 0);
    }

    public void reset(String host) {
        requestCounts.put(host, 0);
    }
}
