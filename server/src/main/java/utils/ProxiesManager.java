package utils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import Demo.CallBackPrx;

public class ProxiesManager {



    private static ProxiesManager instance;

        private Map<String, CallBackPrx> proxiesManager;

    private ProxiesManager() {
        proxiesManager = new ConcurrentHashMap<>();
    }

    public static ProxiesManager getInstance() {
        if (instance == null) {
            synchronized (ProxiesManager.class) {
                if (instance == null) {
                    instance = new ProxiesManager();
                }
            }
        }
        return instance;
    }

    public void addProxy(String key, CallBackPrx proxy) {
        proxiesManager.put(key, proxy);
    }

    public CallBackPrx getProxy(String key) {
        return proxiesManager.get(key);
    }

    public void removeProxy(String key) {
        proxiesManager.remove(key);
    }

    public boolean containsProxy(String key) {
        return proxiesManager.containsKey(key);
    }

    public Map<String, CallBackPrx> getAllProxies() {
        return new HashMap<>(proxiesManager);
    }
    
}
