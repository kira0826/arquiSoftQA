package commands;

import java.util.Map;

import Demo.Response;
import interfaces.Command;

public class ResetCommand implements Command {

    private Map<String, Integer> requestCounts;

    public ResetCommand(Map<String, Integer> requestCounts) {
        this.requestCounts = requestCounts;
    }

    @Override
    public Response execute(String username, String hostname, String[] args) {

        Response response = new Response();
        requestCounts.put(hostname, 0);
        response.value = "Counter cleared";
        response.responseTime = 0;
        return response;
    }




    
    
    
}
