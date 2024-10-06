
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import Demo.Response;
import commands.ExceuteShellCommand;
import commands.FibonacciAndPrimesCommand;
import commands.ListPortsCommands;
import interfaces.Command;
import utils.CommandFactory;

public class PrinterI implements Demo.Printer {

    private Map<String, Integer> requestCounts;
    private CommandFactory commandFactory;

    public PrinterI() {

        requestCounts = new HashMap<>();
        commandFactory = new CommandFactory(requestCounts);
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

}
