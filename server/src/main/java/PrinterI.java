
import java.util.Arrays;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.zeroc.Ice.Current;

import Demo.CallBackPrx;
import Demo.Response;
import commands.ExceuteShellCommand;
import commands.FibonacciAndPrimesCommand;
import commands.ListPortsCommands;
import commands.Command;
import responses.PendingResponse;
import responses.PendingResponseManager;
import responses.UpdateMessagesJob;
import utils.CommandFactory;
import utils.ProxiesManager;

public class PrinterI implements Demo.Printer {


    ExecutorService accumulatedMessagesProcess;
    private Map<String, Integer> requestCounts;
    private CommandFactory commandFactory;

    public PrinterI() {

        requestCounts = new ConcurrentHashMap();
        commandFactory = new CommandFactory(requestCounts);
        accumulatedMessagesProcess = Executors.newFixedThreadPool(3);

    }

    public Response printString(String s, com.zeroc.Ice.Current current) {

        long initTime = System.currentTimeMillis();
        Response response = new Response();



        try {
            String[] parts = s.split(":");
            if (parts.length != 3) {
                response.responseTime = System.currentTimeMillis() - initTime;
                response.value = "Invalid message format. Expected format: 'username:host:command'";
                return response;
            }

            //Get parts of the message

            String username = parts[0];
            String host = parts[1];
            String commandStr = parts[2];

            System.out.println("HOSTNAME: " + host);

            updateRequestCounterByHost(host);

            Command command;
            String[] commandArgs = new String[]{};

            //Define arguments for the command


            if (commandStr.startsWith("!")) {

                String[] partsCommand = commandStr.split(" ");
                String[] args = Arrays.copyOfRange(partsCommand, 1, partsCommand.length);
                commandArgs = args;

            } else if (commandStr.startsWith("bc")) {

                String[] partsCommand = commandStr.split(" ");
                String[] args = Arrays.copyOfRange(partsCommand, 1, partsCommand.length);

                commandArgs = args;

            } else if (commandStr.startsWith("listclients")) {

                String[] partsCommand = commandStr.split(" ");
                String[] args = new String[]{};

                if (partsCommand.length > 0 ){
                    args = Arrays.copyOfRange(partsCommand, 1, partsCommand.length);
                }

                commandArgs = args;

            } else if (commandStr.matches("\\d+")) {

                commandArgs = new String[]{commandStr};

            } else if (commandStr.startsWith("to")) {

                //Forma: to <host> <message>

                String[] partsCommand = commandStr.split(" ");
                String[] args = Arrays.copyOfRange(partsCommand, 1, partsCommand.length);
                commandArgs = args;

            } else if (commandStr.startsWith("listports")) {

                commandArgs = commandStr.split(" ");

                if (commandArgs.length < 2) {
                    response.value = "IP address required for listports command.";
                    response.responseTime = -1;
                    return response;
                }

                commandArgs = Arrays.copyOfRange(commandArgs, 1, commandArgs.length);

            }


            //Execute the command

            try{
                command = commandFactory.getCommand(commandStr.split(" ")[0]);
                response = command.execute(username, host, commandArgs);
                response.responseTime = System.currentTimeMillis() - initTime;
            }catch (Exception e){
                response.value = "Error processing the command: " + e.getMessage();
                response.responseTime = -1;
            }


        } catch (Exception e) {
            response.value = "Error processing the command: " + e.getMessage();
            response.responseTime = -1;
        }

        return response;
    }

    private void updateRequestCounterByHost(String host) {
        requestCounts.put(host, requestCounts.getOrDefault(host, 0) + 1);
    }


    @Override
    public void registerCallback(String hostname, CallBackPrx callBack, Current current) {


        ProxiesManager.getInstance().addProxy(hostname, callBack);

        System.out.println("Total proxies registered:");
        ProxiesManager.getInstance().getAllProxies().forEach((k, v) -> {
            System.out.println("Hostname: " + k + " | Proxy: " + v);
        });


        //Verify if there are pending responses for the client, to do that check pending queue
        Queue<PendingResponse> pendingResponses = PendingResponseManager.getInstance().getPendingResponses(hostname);
        if ( pendingResponses != null && !pendingResponses.isEmpty()) {

            //If there are pending responses, we execute that job on a thread-pool

            UpdateMessagesJob updateMessagesJob = new UpdateMessagesJob(pendingResponses, callBack);

            //Possible unncessary threadpool.
            accumulatedMessagesProcess.execute(updateMessagesJob);

        }


    }

}
