package commands;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import Demo.Response;

public class ListNetworkInterfacesCommand implements Command {

    @Override
    public Response execute(String username, String hostname, String[] args) {

        Response response = new Response(); 

        try {
            response.value = listNetworkInterfaces();
        } catch (java.net.SocketException e) {
            
            e.printStackTrace();
            return null;
        }

        return response;
    
    }

    private String listNetworkInterfaces() throws java.net.SocketException {
        StringBuilder output = new StringBuilder();

        
            // Get network interfaces
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();

                // Get actives interfaces that are not loopback.
                if (networkInterface.isUp() && !networkInterface.isLoopback()) {
                    output.append("Interface: ").append(networkInterface.getDisplayName()).append("\n");

                    // Get addresses for the current interface
                    Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        InetAddress address = addresses.nextElement();
                        output.append("  Address: ").append(address.getHostAddress()).append("\n");
                    }
                }
            }

            return output.toString();
      
    }

}
