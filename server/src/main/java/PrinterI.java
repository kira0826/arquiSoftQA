
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.zeroc.Ice.Current;
import com.zeroc.Ice.InputStream;

import Demo.Response;

public class PrinterI implements Demo.Printer {

    private Map<String, Integer> requestCounts;

    public PrinterI() {

        requestCounts = new ConcurrentHashMap();

    }
    
    @Override
    public Response FibonacciAndPrimesCommand(String hostname, int number, Current current) {

        Response response = new Response();

        // Imprimir la serie de Fibonacci en la consola
        String fibonacciSeries = generateFibonacci(number);

        System.out.println("Fibonacci series for " + number + ": " + fibonacciSeries);

        // Calcular los factores primos de n y devolverlos como respuesta
        List<Integer> primeFactors = getPrimeFactors(number);
        String factores = "Prime factors of " + number + ": " + primeFactors.toString() + "/n";
        System.out.println(factores);
        response.value = "Factors and fibonacci printed on server.";
        updateRequestCounterByHost(hostname);

        return response;

    }

    @Override
    public Response counterRequestCommand(String hostname, Current current) {

        Response response = new Response();
        response.value = String.valueOf(requestCounts.getOrDefault(hostname, 0));
        response.responseTime = 0;

        updateRequestCounterByHost(hostname);

        return response;

    }

    @Override
    public Response exceuteShellCommand(String hostname, String command, Current current) {
        Response response = new Response();
        try {
            response.value = executeCommand(command);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        updateRequestCounterByHost(hostname);
        return response;
    }

    @Override
    public Response listNetworkInterfacesCommand(String hostname, Current current) {
        Response response = new Response(); 

        try {
            response.value = listNetworkInterfaces();
        } catch (java.net.SocketException e) {
            
            e.printStackTrace();
            return null;
        }
        updateRequestCounterByHost(hostname);


        return response;
    
    }

    @Override
    public Response listPortsCommands(String hostname, String ip, Current current) {
        Response response = new Response();
        try {
            response.value = executeCommand("nmap " + ip);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        updateRequestCounterByHost(hostname);
        return response;
    }

    @Override
    public Response resetCommand(String hostname, Current current) {
        Response response = new Response();
        requestCounts.put(hostname, 0);
        response.value = "Counter cleared";
        response.responseTime = 0;
        return response;
    }

    private void updateRequestCounterByHost(String host) {
        requestCounts.put(host, requestCounts.getOrDefault(host, 0) + 1);
    }

    private static String executeCommand(String m) throws IOException {
        String str = null, output = "";
        InputStream s;
        BufferedReader r;

        Process p = Runtime.getRuntime().exec(m);

        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        while ((str = br.readLine()) != null) {
            output += str + System.getProperty("line.separator");
        }
        br.close();
        return output;
    }

    private String generateFibonacci(int n) {
        StringBuilder fibonacciSeries = new StringBuilder();
        int a = 0, b = 1;

        if (n >= 1) {
            fibonacciSeries.append(a).append(" ");
        }
        if (n >= 2) {
            fibonacciSeries.append(b).append(" ");
        }

        for (int i = 3; i <= n; i++) {
            int next = a + b;
            fibonacciSeries.append(next).append(" ");
            a = b;
            b = next;
        }

        return fibonacciSeries.toString();
    }

    private String listNetworkInterfaces() throws java.net.SocketException {
        StringBuilder output = new StringBuilder();

        // Get network interfaces
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();

            // Get actives interfaces that are not loopback.
            if (networkInterface.isUp() && !networkInterface.isLoopback()) {
                output.append("Interface: ").append(networkInterface.getDisplayName()).append("\n");

                // Get addresses for the current interface
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    output.append("  Address: ").append(address.getHostAddress()).append("\n");
                }
            }
        }

        return output.toString();

    }

    // Método para obtener los factores primos de un número
    private List<Integer> getPrimeFactors(int n) {
        List<Integer> primeFactors = new ArrayList<>();

        // Dividir n por 2 mientras sea divisible
        while (n % 2 == 0) {
            primeFactors.add(2);
            n /= 2;
        }

        // Dividir n por números impares
        for (int i = 3; i <= Math.sqrt(n); i += 2) {
            while (n % i == 0) {
                primeFactors.add(i);
                n /= i;
            }
        }

        // Si n es un número primo mayor que 2
        if (n > 2) {
            primeFactors.add(n);
        }

        return primeFactors;
    }
    
}
