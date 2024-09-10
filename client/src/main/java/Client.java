import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.zeroc.Ice.InputStream;

import Demo.Response;

public class Client {

    private static int sentRequestCount = 0;
    private static int receivedResponseCount = 0;
    private static List<Long> responseTimes = new ArrayList<Long>();

    public static void main(String[] args) {
        java.util.List<String> extraArgs = new java.util.ArrayList<>();

        Scanner scanner = new Scanner(System.in);

        try (com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize(args, "config.client",
                extraArgs)) {
            // com.zeroc.Ice.ObjectPrx base =
            // communicator.stringToProxy("SimplePrinter:default -p 10000");
            Response response = null;
            Demo.PrinterPrx service = Demo.PrinterPrx
                    .checkedCast(communicator.propertyToProxy("Printer.Proxy"));

            if (service == null) {
                throw new Error("Invalid proxy");
            }

            String message = "";
            String whoami = f("whoami");
            String hostname = "";
            String input = "";
            if (whoami.equals("error")) {
                System.out.println("Error al obtener el usuario");
                return;
            }
            try {
                hostname = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }

            while (true) {
                System.out.println("Modo de prueba:\n1. Unitario\n2. Benchmark");
                int mode = Integer.parseInt(scanner.nextLine());
                if (mode == 2) {
                    System.out.println("Ingrese la cantidad de request a enviar: ");
                    int amount = Integer.parseInt(scanner.nextLine());
                    System.out.println("Ingrese el mensaje a enviar: ");
                    String msg = scanner.nextLine();
                    benchmark(amount, msg, service);
                    return;
                }

                message += whoami + ":" + hostname + ":";
                System.out.println("Ingrese un mensaje para enviar al servidor: ");
                input = scanner.nextLine();
                if (input.equals("exit"))
                    return;
                sentRequestCount++;
                response = service.printString(message + input);
                receivedResponseCount++;
                responseTimes.add(response.responseTime);

                System.out.println("Respuesta desde el server: " + response.value);
                System.out.println("Tiempo de respuesta: " + response.responseTime);
                stats(askForServerCount(service));
            }

        }
    }

    public static void benchmark(int amount, String input, Demo.PrinterPrx service) {
        reset();
        String message = "";
        String whoami = f("whoami");
        String hostname = "";
        message += whoami + ":" + hostname + ":";
        Response response = null;
        for (int i = 0; i < amount; i++) {
            System.out.println("Enviando request " + i);
            sentRequestCount++;
            response = service.printString(message + input);
            responseTimes.add(response.responseTime);
            receivedResponseCount++;
        }
        System.out.println(showResponseTimes());
        stats(askForServerCount(service));
        System.out.println("Benchmark finalizado");
        reset();
    }

    public static void reset() {
        sentRequestCount = 0;
        receivedResponseCount = 0;
        responseTimes = new ArrayList<Long>();
    }

    public static String showResponseTimes() {
        String response = "";
        for (int i = 0; i < responseTimes.size(); i++) {
            response += i + ". " + responseTimes.get(i) + "\n";
        }
        return response;
    }

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

}