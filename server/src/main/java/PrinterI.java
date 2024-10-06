
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
import interfaces.Command;
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
        accumulatedMessagesProcess= Executors.newFixedThreadPool(3);

    }

    public Response printString(String s, com.zeroc.Ice.Current current) {
       
        long initTime = System.currentTimeMillis();

        //Register callback only if the reference is not the same as the previous one



        Response response = new Response();

        try {
            String[] parts = s.split(":");
            if (parts.length != 3) {
                response.responseTime = System.currentTimeMillis() - initTime;
                response.value = "Invalid message format. Expected format: 'username:host:command'";
                return response;
            }

            String username = parts[0];
            String host = parts[1];
            String commandStr = parts[2];

            updateRequestCounterByHost(host);

            Command command;
            String[] commandArgs;

            if (commandStr.startsWith("!")) {

                command = new ExceuteShellCommand();
                commandArgs = new String[]{commandStr.substring(1)};

            } else if (commandStr.matches("\\d+")) {

                command = new FibonacciAndPrimesCommand();
                commandArgs = new String[]{commandStr};

            }else if(commandStr.startsWith("to")){

                //Forma: to <host> <message>

                String[] partsCommand = commandStr.split(" ");
                String[] args = Arrays.copyOfRange(partsCommand, 1, partsCommand.length);
                command = commandFactory.getCommand(partsCommand[0]);
                commandArgs = args;

            } else if (commandStr.startsWith("listports")) {

                command = new ListPortsCommands();
                commandArgs = commandStr.split(" ");

                if (commandArgs.length < 2) {
                    response.value = "IP address required for listports command.";
                    response.responseTime = -1;
                    return response;
                }

                commandArgs = Arrays.copyOfRange(commandArgs, 1, commandArgs.length);

            } else {

                command = commandFactory.getCommand(commandStr);
                commandArgs = new String[]{};

            }

            if (command != null) {

                //Here we contemplate the reset and resquest counter commands
                response = command.execute(username, host, commandArgs);
                response.responseTime = System.currentTimeMillis() - initTime;

            } else {

                response.value = "Unrecognized command: " + commandStr;
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
        System.out.println("Cliente registrado: " + hostname);

        ProxiesManager.getInstance().getAllProxies().forEach((k, v) -> {
            System.out.println("Cliente: " + k) ;
            System.out.println("Proxie: " + v) ;

        });     

        if(PendingResponseManager.getInstance().getPendingResponses(hostname) != null){

        
            Queue<PendingResponse> pendingResponses = PendingResponseManager.getInstance().getPendingResponses(hostname);
            
            UpdateMessagesJob updateMessagesJob = new UpdateMessagesJob(pendingResponses, callBack);
            
            accumulatedMessagesProcess.execute(updateMessagesJob);

        }


    }

}
