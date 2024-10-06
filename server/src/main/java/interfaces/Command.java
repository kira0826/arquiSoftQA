package interfaces;

import Demo.Response;

public interface Command {
    Response execute(String username, String hostname, String[] args); 
}
