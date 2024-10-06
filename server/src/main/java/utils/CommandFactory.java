package utils;

import java.util.HashMap;
import java.util.Map;

import commands.CounterRequestCommand;
import commands.FibonacciAndPrimesCommand;
import commands.ListNetworkInterfacesCommand;
import commands.ListPortsCommands;
import commands.ResetCommand;
import interfaces.Command;

public class CommandFactory {

    private Map<String, Command> commandMap = new HashMap<>();

    public CommandFactory(Map<String, Integer> requestCounts) {

        commandMap.put("listifs", new ListNetworkInterfacesCommand());
        commandMap.put("listports", new ListPortsCommands());
        commandMap.put("fibonnaciAndPrimes", new FibonacciAndPrimesCommand());
        commandMap.put("counterRequest", new CounterRequestCommand(requestCounts));
        commandMap.put("reset", new ResetCommand(requestCounts));

    }

    public Command getCommand(String commandKey) {
        return commandMap.get(commandKey);
    }

}
