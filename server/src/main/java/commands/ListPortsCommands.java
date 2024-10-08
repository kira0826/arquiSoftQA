package commands;

import java.io.IOException;

import Demo.Response;
import utils.CommonCommand;

public class ListPortsCommands implements Command{

    /***
     * This method will execute the command to list the ports of a host. It must recieve in args
     * the host to be scanned.
     */
    @Override
    public Response execute(String username, String hostname, String[] args) {

        
        Response response = new Response();
        try {
            response.value = CommonCommand.executeCommand("nmap " + args[0]);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return response;
    }




}
