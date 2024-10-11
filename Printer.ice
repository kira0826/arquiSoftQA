module Demo
{
    class Response{
        long responseTime;
        string value;
    }
    

    interface CallBack

    {
        void reportResponse(Response response);
    }

    interface ChatHandler

    {
        
        Response registerCallback(string hostname, CallBack* callBack);
        
        Response sendOneMessage(string hostname, string receiver,  string message);

        Response broadcast(string hostname, string message);

        Response listClients(string hostname);

    }


    interface Printer
    {

        Response counterRequestCommand(string hostname);

        Response exceuteShellCommand(string hostname, string command);

        Response FibonacciAndPrimesCommand(string hostname, int number);

        Response listNetworkInterfacesCommand(string hostname );

        Response listPortsCommands(string hostname, string ip);

        Response resetCommand(string hostname);
        
    }

}