import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.stream.Collectors;

import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.ObjectPrx;
import com.zeroc.Ice.Util;

import Demo.Response;

public class Client {

    private static Scanner scanner = new Scanner(System.in);

    private static List<Long> processingTimes = new ArrayList<Long>();
    private static List<Long> responseTimes = new ArrayList<Long>();

    private static int sentRequestCount = 0;
    private static int receivedResponseCount = 0;

    private static long start = 0;
    private static long end = 0;

    public static void main(String[] args) throws IOException {

        // Obtén la dirección IP en el segmento 10.147.19
        String clientIP = Collections.list(NetworkInterface.getNetworkInterfaces()).stream()
                .flatMap(intf -> Collections.list(intf.getInetAddresses()).stream())
                .filter(addr -> addr.getHostAddress().startsWith("10.147.19"))
                .map(InetAddress::getHostAddress)
                .findFirst()
                .orElse("localhost");
        System.out.println("Client IP: " + clientIP);
        // Crear una lista de configuraciones dinámicamente
        String[] iceArgs = {
                "--Printer.Proxy=SimplePrinter:tcp -p 9099", // Proxy de servicio
                "--CallBack.Endpoints=tcp -h " + clientIP, // Endpoints dinámicos
                "--Ice.Default.Host=" + "xhgrid5", // Establece la IP del cliente
                "--Ice.ThreadPool.Server.Size=6", // Configuración del tamaño del thread pool
        };
        java.util.List<String> extraArgs = new java.util.ArrayList<>();

        try (com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize(iceArgs, extraArgs)) {

            // ICE CONFIGURATION
            communicator.getProperties().setProperty("Printer.Proxy", "SimplePrinter:tcp -p 9099");
            communicator.getProperties().setProperty("CallBack.Endpoints", "tcp -h " + clientIP);
            communicator.getProperties().setProperty("Ice.Default.Host", "xhgrid5");
            communicator.getProperties().setProperty("Ice.ThreadPool.Server.Size", "6");

            Demo.PrinterPrx service = Demo.PrinterPrx
                    .checkedCast(communicator.propertyToProxy("Printer.Proxy"));

            // Create adapter to expose the callback object
            ObjectAdapter adapter = communicator.createObjectAdapter("CallBack");

            CallbackI callbackI = new CallbackI();
            Demo.CallBack callBack = callbackI;

            // Cast the proxy to the correct type
            ObjectPrx prx = adapter.add(callBack, Util.stringToIdentity("Callback"));
            Demo.CallBackPrx callBackPrx = Demo.CallBackPrx.checkedCast(prx);

            // String hostnameForProxy = "";
            // try {
            // hostnameForProxy = InetAddress.getLocalHost().getHostAddress();
            // } catch (UnknownHostException e) {
            // // TODO Auto-generated catch block
            // e.printStackTrace();
            // }

            System.out.println("Dame el identificador del proxy: ");
            String temHost = scanner.nextLine();
            System.out.println("TemHost: " + temHost);
            // System.out.println("Hostname for proxy: " + hostnameForProxy);

            // register the callback
            service.registerCallback(temHost, callBackPrx);

            adapter.activate();

            if (service == null) {
                throw new Error("Invalid proxy");
            }
            try {
                while (true) {
                    System.out.println("Modo de prueba:\n0. Salir\n1. Unitario\n2. Benchmark\n3. Throughput");
                    int mode = Integer.parseInt(scanner.nextLine());
                    switch (mode) {
                        case 0:
                            System.exit(0);
                            break;
                        case 1:
                            unitario(service, callbackI);
                            break;
                        case 2:
                            benchmark(service);
                            break;
                        case 3:
                            throughput(service);
                            break;
                        default:
                            break;
                    }
                }
            } catch (NoSuchElementException e) {

            }
        }
    }

    // Modos de prueba
    // 1. Unitario

    public static void unitario(Demo.PrinterPrx service, CallbackI callbackI) {
        // Inicializacion de variables (mensaje y userHostname)
        String userHostname = setUserHostname();
        System.out.println("UserHostname: " + userHostname);
        System.out.println("Ingrese un mensaje para enviar al servidor: ");
        String input = scanner.nextLine();
        if (input.equals("exit"))
            return;

        sentRequestCount++;
        start = System.currentTimeMillis();
        Response response = service.printString(userHostname + input);
        end = System.currentTimeMillis();
        receivedResponseCount++;
        processingTimes.add(response.responseTime);
        Long responseTime = end - start;
        responseTimes.add(responseTime);

        // Show stats
        System.out.println("Respuesta desde el server: " + response.value);
        System.out.println("Tiempo de procesamiento: " + response.responseTime);
        System.out.println("Tiempo de respuesta: " + responseTime);
        System.out.println("Mensajes llegados al CallbackI: " + callbackI.getCounter());
        stats(askForServerCount(service));
    }

    // 2. Benchmark

    public static void benchmark(Demo.PrinterPrx service) {
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
            response = service.printString(userHostname + input);
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
        stats(askForServerCount(service));
        System.out.println("Benchmark finalizado");
        reset();
    }

    // 3. Throughput

    public static void throughput(Demo.PrinterPrx service) {
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
            response = service.printString(userHostname + input);
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

    // reset stats

    public static void reset() {
        sentRequestCount = 0;
        receivedResponseCount = 0;
        processingTimes = new ArrayList<Long>();
    }

    // Ask server for received requests counter

    public static int askForServerCount(Demo.PrinterPrx service) {
        String message = "";
        String whoami = f("whoami");
        String hostname = "";
        message += whoami + ":" + hostname + ":";
        Response response = null;
        response = service.printString(message + "counterRequest");
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