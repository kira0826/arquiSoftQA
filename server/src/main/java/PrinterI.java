import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zeroc.Ice.InputStream;

import Demo.Response;

public class PrinterI implements Demo.Printer{

    private Map<String, Integer> requestCounts = new HashMap<>();

    public Response printString(String s, com.zeroc.Ice.Current current)
{
        Long initTime = System.currentTimeMillis();

        Response response = new Response();
        
        try {
            // Descomponer el string en username:host:command
            String[] parts = s.split(":");
            if (parts.length != 3) {
                // Si el formato no es correcto, devolvemos un mensaje de error
                response.responseTime = System.currentTimeMillis();
                response.value = "Invalid message format. Expected format: 'username:host:command'";
                return response;
            }

            //Partición del mensaje

            String username = parts[0];
            String host = parts[1];
            String command = parts[2];

            //Conteo de reques de un host en particular.

            updateRequestCounterByHost(host);
            
            // Encadenar if-else para manejar los distintos comandos

        
            if (command.equals("listifs")) {

                response.value = listNetworkInterfaces();
                response.responseTime = System.currentTimeMillis() - initTime;


            } else if (command.startsWith("listports")) {

                String[] values =  command.split(" ");


                response.value = executeCommand("nmap " + values[1]);

            } else if (command.matches("\\d+")) {

                int n = Integer.parseInt(command);
                
                // Imprimir la serie de Fibonacci en la consola
                String fibonacciSeries = generateFibonacci(n);
                System.out.println(username + "/" + host + " - Fibonacci series for " + n + ": " + fibonacciSeries);

                // Calcular los factores primos de n y devolverlos como respuesta
                List<Integer> primeFactors = getPrimeFactors(n);
                String factores = "Prime factors of " + n + ": " + primeFactors.toString() + "/n";
                System.out.println(factores);

                response.value = "Factors and fibonacci printed on server.";
                response.responseTime = System.currentTimeMillis() - initTime;


            } else if (command.startsWith("!")) {
                // Si empieza con '!', extraemos el comando para ejecutar
                String execCommand = command.substring(1);  

                response.value = executeCommand(execCommand);
                response.responseTime = System.currentTimeMillis() - initTime;
            
            

            }else if(command.equals("counterRequest")) {

                response.value = String.valueOf( requestCounts.get(host));
                response.responseTime = 0;


            } else if(command.equals("reset")){

                requestCounts.put(host, 0);

                response.value = "Counter cleared";
                response.responseTime = 0;

            }else {
                // Comando no reconocido
                response.value = "Unrecognized command: " + command;
                response.responseTime = -1;
                
            }
        } catch (Exception e) {
            // En caso de error, devolvemos un mensaje de error
            response.value = "Error processing the command: " + e.getMessage();
            response.responseTime = -1;

        }

        return response;

      
    }



       public String  executeCommand(String m){
        String str = null, output = "";
        InputStream s;
        BufferedReader r;

        try {
            Process p = Runtime.getRuntime().exec(m);

            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream())); 
            while ((str = br.readLine()) != null) 
            output += str + System.getProperty("line.separator"); 
            br.close(); 
            return output;
        }
        catch(Exception ex) {

            return "Problema";
        }
    }


    private void updateRequestCounterByHost(String host){

        if(requestCounts.get(host)!= null){
                
            int requestCounter = requestCounts.get(host);
            
            requestCounts.put(host, requestCounter+1);


        }else{
            requestCounts.put(host, 1);
            
        }

        requestCounts.put(host,1);
        
    }

    private String listNetworkInterfaces() {
        StringBuilder output = new StringBuilder();

        try {
            // Obtener todas las interfaces de red
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                
                // Solo considerar interfaces que estén activas y no sean loopback
                if (networkInterface.isUp() && !networkInterface.isLoopback()) {
                    output.append("Interface: ").append(networkInterface.getDisplayName()).append("\n");

                    // Obtener las direcciones IP asociadas a esta interfaz
                    Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        InetAddress address = addresses.nextElement();
                        output.append("  Address: ").append(address.getHostAddress()).append("\n");
                    }
                }
            }

            return output.toString();
        } catch (Exception e) {
            return "Error listing network interfaces: " + e.getMessage();
        }
    }


    private String generateFibonacci(int n) {
        StringBuilder fibonacciSeries = new StringBuilder();

        // Usar BigInteger en lugar de int
        BigInteger a = BigInteger.ZERO;
        BigInteger b = BigInteger.ONE;

        if (n >= 1) fibonacciSeries.append(a).append(" ");
        if (n >= 2) fibonacciSeries.append(b).append(" ");

        for (int i = 3; i <= n; i++) {
            BigInteger next = a.add(b);  // a + b usando BigInteger
            fibonacciSeries.append(next).append(" ");
            a = b;
            b = next;
        }

        return fibonacciSeries.toString();
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