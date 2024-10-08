package utils;

import java.util.HashMap;
import java.util.Map;

import commands.*;

public class CommandFactory {

    private Map<String, Command> commandMap = new HashMap<>();

    public CommandFactory(Map<String, Integer> requestCounts) {

        commandMap.put("listifs", new ListNetworkInterfacesCommand());
        commandMap.put("listports", new ListPortsCommands());
        commandMap.put("fibonnaciAndPrimes", new FibonacciAndPrimesCommand());
        commandMap.put("counterRequest", new CounterRequestCommand(requestCounts));
        commandMap.put("reset", new ResetCommand(requestCounts));
        commandMap.put("to", new SendOneMessage());
        commandMap.put("listclients", new ListClients());
        commandMap.put("bc", new Broadcast());
        commandMap.put("!", new ExceuteShellCommand());

    }

    public Command getCommand(String commandKey) {
        return commandMap.get(commandKey);
    }

}
