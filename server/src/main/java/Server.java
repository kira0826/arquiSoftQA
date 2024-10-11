import com.zeroc.Ice.ObjectAdapter;

public class Server {

    public static void main(String[] args) {
        java.util.List<String> extraArgs = new java.util.ArrayList<String>();

        try (com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize(args, "config.server",
                extraArgs)) {
            if (!extraArgs.isEmpty()) {
                System.err.println("too many arguments");
                for (String v : extraArgs) {
                    System.out.println(v);
                }
            }
            com.zeroc.Ice.ObjectAdapter adapter = communicator.createObjectAdapter("Printer");
            com.zeroc.Ice.Object object = new PrinterI();
            //FALTA EXPONER EL CHATSERVER.
            adapter.add(object, com.zeroc.Ice.Util.stringToIdentity("SimplePrinter"));
            adapter.activate();

            ObjectAdapter chatHandlerAdapter = communicator.createObjectAdapter("ChatHandler");
            ChatHandler chatHandlerObject = new ChatHandler(); // La clase que implementa ChatHandler
            chatHandlerAdapter.add(chatHandlerObject, com.zeroc.Ice.Util.stringToIdentity("SimpleChatHandler"));
            chatHandlerAdapter.activate();

            communicator.waitForShutdown();
        }
    }

}