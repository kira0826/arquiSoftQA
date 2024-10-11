import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;

import com.zeroc.Ice.InputStream;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.ObjectPrx;
import com.zeroc.Ice.Util;

import Demo.Response;
import utils.NetworkUtils;

public class Client {

    private static Scanner scanner = new Scanner(System.in);

    private static List<Long> processingTimes = new ArrayList<Long>();
    private static List<Long> responseTimes = new ArrayList<Long>();

    private static int sentRequestCount = 0;
    private static int receivedResponseCount = 0;

    private static long start = 0;
    private static long end = 0;

    public static void main(String[] args) {
        java.util.List<String> extraArgs = new java.util.ArrayList<>();

        try (com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize(args, "config.client",
                extraArgs)) {

            // Verify current IP
            String ip = NetworkUtils.getLocalIPAddress();
            System.out.println("Current IP: " + ip);

            // Set current client server ip

            communicator.getProperties().setProperty("CallBack.Endpoints", "tcp -h " + ip);
            System.out.println(communicator.getProperties().getProperty("CallBack.Endpoints"));

            Demo.PrinterPrx printerService = Demo.PrinterPrx
                    .checkedCast(communicator.propertyToProxy("Printer.Proxy"));

            Demo.ChatHandlerPrx chatService = Demo.ChatHandlerPrx
                    .checkedCast(communicator.propertyToProxy("ChatHandler.Proxy"));

            // Create adapter to expose the callback object
            ObjectAdapter adapter = communicator.createObjectAdapter("CallBack");
            Demo.CallBack callBack = new CallbackI();

            // Cast the proxy to the correct type
            ObjectPrx prx = adapter.add(callBack, Util.stringToIdentity("Callback"));
            Demo.CallBackPrx callBackPrx = Demo.CallBackPrx.checkedCast(prx);

            // service.registerCallback(ip, callBackPrx);

            adapter.activate();

            try {
                while (true) {
                    System.out.println("Modo de prueba:\n0. Salir\n1. Unitario\n2. Benchmark\n3. Throughput");
                    int mode = Integer.parseInt(scanner.nextLine());
                    System.out.println("Modo seleccionado: " + mode);
                    switch (mode) {
                        case 0:
                            System.exit(0);
                            break;
                        case 1:
                            unitario(printerService, chatService);
                            break;
                        case 2:
                            benchmark(printerService, chatService);
                            break;
                        case 3:
                            throughput(printerService, chatService);
                            break;
                        default:
                            break;
                    }
                }
            } catch (NoSuchElementException e) {

            }
            communicator.waitForShutdown();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    public static Response execCommand(String s, Demo.PrinterPrx printerService, Demo.ChatHandlerPrx chatService) {
        long initTime = System.currentTimeMillis();
        Response response = new Response();

        try {
            String[] parts = s.split(":");
            if (parts.length != 3) {
                response.responseTime = System.currentTimeMillis() - initTime;
                response.value = "Invalid message format. Expected format: 'username:host:command'";
                return null;
            }

            // Get parts of the message

            String username = parts[0];
            String host = parts[1];
            String commandStr = parts[2];

            System.out.println("HOSTNAME: " + host);

            // Define arguments for the command

            if (commandStr.startsWith("!")) {

                response = printerService.exceuteShellCommand(host, commandStr.substring(1));

            } else if (commandStr.startsWith("bc")) {

                response = chatService.broadcast(host, commandStr.substring(3));

            } else if (commandStr.startsWith("listclients")) {

                response = chatService.listClients(host);

            } else if (commandStr.matches("\\d+")) {

                response = printerService.FibonacciAndPrimesCommand(host, Integer.parseInt(commandStr));

            } else if (commandStr.startsWith("to")) {

                String[] partsCommand = commandStr.split(" ");
                String receiver = partsCommand[1];

                String[] args = Arrays.copyOfRange(partsCommand, 2, partsCommand.length);
                String messageToSend = String.join(" ", args);

                response = chatService.sendOneMessage(host, receiver, messageToSend);

            } else if (commandStr.startsWith("listports")) {

                String[] args = commandStr.split(" ");

                if (args.length < 2) {
                    response.value = "IP address required for listports command.";
                    response.responseTime = -1;
                    return null;
                }

                response = printerService.listPortsCommands(host, args[1]);

            }

        } catch (Exception e) {
            response.value = "Error processing the command: " + e.getMessage();
            response.responseTime = -1;
        }

        System.out.println("Response: " + response.value);

        return response;
    }

    // Modos de prueba
    // 1. Unitario

    public static void unitario(Demo.PrinterPrx printerPrxService, Demo.ChatHandlerPrx chatService) {

        // Inicializacion de variables (mensaje y userHostname)
        String userHostname = setUserHostname();

        System.out.println("HOSTANME UNITARIO: " + userHostname);
        System.out.println("Ingrese un mensaje para enviar al servidor: ");
        String input = scanner.nextLine();
        System.out.println("INPUT " + input);

        if (input.equals("exit"))
            return;

        sentRequestCount++;
        start = System.currentTimeMillis();
        Response response = execCommand(userHostname + input, printerPrxService, chatService);
        end = System.currentTimeMillis();
        receivedResponseCount++;
        processingTimes.add(response.responseTime);
        Long responseTime = end - start;
        responseTimes.add(responseTime);

        // Show stats
        System.out.println("Respuesta desde el server: " + response.value);
        System.out.println("Tiempo de procesamiento: " + response.responseTime);
        System.out.println("Tiempo de respuesta: " + responseTime);
        stats(askForServerCount(printerPrxService));
    }

    // 2. Benchmark

    public static void benchmark(Demo.PrinterPrx printerPrxService, Demo.ChatHandlerPrx chatService) {
        reset();
        // Inicializacion de variables (mensaje, cantidad de request y userHostname)
        System.out.println("Ingrese la cantidad de request a enviar: ");
        int requestAmount = Integer.parseInt(scanner.nextLine());
        System.out.println("Ingrese el mensaje a enviar: ");
        String input = scanner.nextLine();
        String userHostname = setUserHostname();

        long start = 0;
        long end = 0;
        long responseTime = 0;

        Response response = null;
        for (int i = 0; i < requestAmount; i++) {
            System.out.println("Enviando request " + i);
            sentRequestCount++;
            start = System.currentTimeMillis();
            response = execCommand(userHostname + input, printerPrxService, chatService);
            end = System.currentTimeMillis();
            responseTime = end - start;
            responseTimes.add(responseTime);
            processingTimes.add(response.responseTime);
            receivedResponseCount++;
        }

        // Show stats
        System.out.println("Tiempos de respuesta");
        System.out.println(showResponseTimes());
        System.out.println("Tiempos de procesamiento");
        System.out.println(showProcessingTimes());
        stats(askForServerCount(printerPrxService));
        System.out.println("Benchmark finalizado");
        reset();
    }

    // 3. Throughput

    public static void throughput(Demo.PrinterPrx printerPrxService, Demo.ChatHandlerPrx chatService) {
        // Inicializacion de variables (mensaje, cantidad de tiempo y userHostname)
        System.out.println("Ingrese la cantidad de tiempo a realizar: ");
        int timeAmount = Integer.parseInt(scanner.nextLine());
        System.out.println("Ingrese el mensaje a enviar: ");
        String input = scanner.nextLine();
        String userHostname = setUserHostname();

        int requestCount = 0;

        Response response = null;
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < timeAmount * 1000) {
            response = execCommand(userHostname + input, printerPrxService, chatService);
            requestCount++;
        }

        long elapsedTime = System.currentTimeMillis() - startTime;
        double throughput = (double) requestCount / (elapsedTime / 1000.0);

        // Show stats

        System.out.println("Total de peticiones completadas: " + requestCount);
        System.out.println("Tiempo total transcurrido (ms): " + elapsedTime);
        System.out.println("Throughput (peticiones por segundo): " + throughput);
    }

    // set hostname + whoami

    public static String setUserHostname() {

        String whoami = f("whoami");
        String hostname = "";

        if (whoami.equals("error")) {
            System.out.println("Error al obtener el usuario");
            return "error";
        }

        try {
            hostname = Collections.list(NetworkInterface.getNetworkInterfaces()).stream()
                    .flatMap(intf -> Collections.list(intf.getInetAddresses()).stream())
                    .filter(addr -> addr.getHostAddress().startsWith("10.147.19"))
                    .findFirst()
                    .orElse(InetAddress.getLocalHost())
                    .getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        }

        return whoami + ":" + hostname + ":";

    }

    public static String setHostname() {
        String hostname = "";
        try {
            hostname = Collections.list(NetworkInterface.getNetworkInterfaces()).stream()
                    .flatMap(intf -> Collections.list(intf.getInetAddresses()).stream())
                    .filter(addr -> addr.getHostAddress().startsWith("10.147.19"))
                    .findFirst()
                    .orElse(InetAddress.getLocalHost())
                    .getHostAddress();
            return hostname;
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return "error";
        } catch (SocketException e) {
            e.printStackTrace();
            return "error";
        }
    }

    // reset stats

    public static void reset() {
        sentRequestCount = 0;
        receivedResponseCount = 0;
        processingTimes = new ArrayList<Long>();
    }

    // Ask server for received requests counter

    public static int askForServerCount(Demo.PrinterPrx service) {
        String hostname = setHostname();
        Response response = null;
        response = service.counterRequestCommand(hostname);
        return Integer.parseInt(response.value);
    }

    public static void stats(int serverCounter) {
        System.out.println("Cantidad de request enviados: " + sentRequestCount);
        System.out.println("Cantidad de request recibidos: " + receivedResponseCount);
        System.out.println("Cantidad de request perdidos: "
                + ((sentRequestCount - serverCounter) / sentRequestCount) * 100 + "%");
        System.out.println("Porcentaje de request sin procesar: "
                + (((sentRequestCount - receivedResponseCount) - (sentRequestCount - serverCounter))
                        / sentRequestCount) * 100
                + "%");
        System.out.println("Jitter: " + calcularDesviacionEstandar(responseTimes));
    }

    public static String f(String m) {
        String str = null, output = "";

        InputStream s;
        BufferedReader r;

        try {
            Process p = Runtime.getRuntime().exec(m);
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((str = br.readLine()) != null)
                output += str;// + System.getProperty("line.separator");
            br.close();
            return output;
        } catch (Exception ex) {
            return "error";
        }
    }

    public static double calcularDesviacionEstandar(List<Long> datosLong) {
        double[] datos = new double[datosLong.size()];
        // Iterar sobre la lista y convertir cada elemento a double
        for (int i = 0; i < datosLong.size(); i++) {
            datos[i] = datosLong.get(i).doubleValue();
        }
        double media = calcularMedia(datos);
        double suma = 0;
        // Sumar los cuadrados de las diferencias respecto a la media
        for (double num : datos) {
            suma += Math.pow(num - media, 2);
        }
        // Calcular la varianza dividiendo la suma por la cantidad de datos
        double varianza = suma / datos.length;
        // Devolver la raíz cuadrada de la varianza (desviación estándar)
        return Math.sqrt(varianza);
    }

    // Método para calcular la media de un array de datos
    public static double calcularMedia(double[] datos) {
        double suma = 0;
        for (double num : datos) {
            suma += num;
        }
        return suma / datos.length;
    }

    public static String showProcessingTimes() {
        String response = "";
        for (int i = 0; i < processingTimes.size(); i++) {
            response += i + ". " + processingTimes.get(i) + "\n";
        }
        return response;
    }

    public static String showResponseTimes() {
        String response = "";
        for (int i = 0; i < responseTimes.size(); i++) {
            response += i + ". " + responseTimes.get(i) + "\n";
        }
        return response;
    }

}