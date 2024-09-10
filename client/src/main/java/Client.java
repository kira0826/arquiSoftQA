import Demo.Response;

public class Client
{
    public static void main(String[] args)
    {
        java.util.List<String> extraArgs = new java.util.ArrayList<>();

        try(com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize(args,"config.client",extraArgs))
        {
            //com.zeroc.Ice.ObjectPrx base = communicator.stringToProxy("SimplePrinter:default -p 10000");
            Response response = null;
            Demo.PrinterPrx service = Demo.PrinterPrx
                    .checkedCast(communicator.propertyToProxy("Printer.Proxy"));
            
            if(service == null)
            {
                throw new Error("Invalid proxy");
            }
            response = service.printString("Hello World from a remote client!");

            System.out.println("Respuesta del server: " + response.value + ", " + response.responseTime);
        }
    }
}