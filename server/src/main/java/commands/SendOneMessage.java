package commands;

import com.zeroc.Ice.ConnectionRefusedException;

import Demo.CallBackPrx;
import Demo.Response;
import interfaces.Command;
import responses.PendingResponseManager;
import responses.PendingResponseSequential;
import utils.ProxiesManager;

public class SendOneMessage implements Command {

    @Override
    public Response execute(String username, String hostname, String[] args) {

        //Get if exist the proxy to send the message
        Response response = new Response();

        CallBackPrx callBackPrx = ProxiesManager.getInstance().getProxy(args[0]);

        if (callBackPrx != null) {

            String message = "Message from: " + args[0] + " | Message:" +  args[1]; 

            try {


                callBackPrx.reportResponse(message);

                response.value = "Message sent to " + args[0];

            } catch (ConnectionRefusedException e) {
                e.printStackTrace();
                

                response.value = "Error sending message to " + args[0];


                PendingResponseSequential pendingResponse = new PendingResponseSequential();

                pendingResponse.setResponse(message);
                pendingResponse.setInitialSender(hostname);
            
                PendingResponseManager.getInstance().addPendingResponse(args[0], null);


            }catch (Exception e) {
                e.printStackTrace();
                response.value = "Error sending message to " + args[0];
            }

        }

        return response;

    }

}
