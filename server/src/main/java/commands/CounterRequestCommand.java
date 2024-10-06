package commands;

import java.util.Map;

import Demo.Response;
import interfaces.Command;

public class CounterRequestCommand implements Command {

    private Map<String, Integer> requestCounts;

    public CounterRequestCommand(Map<String, Integer> requestCounts) {
        this.requestCounts = requestCounts;
    }

    @Override
    public Response execute(String username, String hostname, String[] args) {
        Response response = new Response();
        response.value = String.valueOf(requestCounts.getOrDefault(hostname, 0));
        response.responseTime = 0;
        return response;
    }

}
