package commands;

import Demo.Response;
import utils.CommonCommand;

public class ExceuteShellCommand implements Command{

    @Override
    public Response execute(String username, String hostname, String[] args) {
        
        Response response = new Response(); 
        try {
            response.value = CommonCommand.executeCommand(args[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return response;

    }
    


    
}
