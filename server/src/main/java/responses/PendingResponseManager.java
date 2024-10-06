package responses;

import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class PendingResponseManager {
    private static PendingResponseManager instance;

    private ConcurrentHashMap<String, Queue<PendingResponse>> pendingResponses;

    private PendingResponseManager() {
        pendingResponses = new ConcurrentHashMap<>();
    }


    public static synchronized PendingResponseManager getInstance() {
        if (instance == null) {
            instance = new PendingResponseManager();
        }
        return instance;
    }

    public void addPendingResponse(String key, PendingResponse response) {
        pendingResponses.computeIfAbsent(key, k -> new LinkedBlockingQueue<>()).offer(response);
    }

    public Queue<PendingResponse> getPendingResponses(String key) {
        return pendingResponses.get(key);
    }

    public void removePendingResponse(String key, PendingResponse response) {
        Queue<PendingResponse> queue = pendingResponses.get(key);
        if (queue != null) {
            queue.remove(response);
        }
    }
}
