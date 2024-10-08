package commands;

import Demo.CallBackPrx;
import Demo.Response;
import responses.PendingResponse;
import responses.PendingResponseManager;
import responses.PendingResponseSequential;
import utils.ProxiesManager;

public class ListClients implements Command {


    @Override
    public Response execute(String username, String hostname, String[] args) {


        Response response = new Response();

        CallBackPrx callBackPrx;

        if (args.length > 0) {

            callBackPrx = ProxiesManager.getInstance().getProxy(args[0]);
        } else {
            callBackPrx = ProxiesManager.getInstance().getProxy(hostname);
        }

        String user = args.length > 0 ? args[0] : hostname;

        if (callBackPrx != null) {

            String message =ProxiesManager.getInstance().getAllProxies().toString();


            try{

                callBackPrx.reportResponse(message);
                response.value = "List clients process sent ";

            } catch (Exception e ) {

                //If the receiver is not connected.
                System.out.println("Error sending message," + user + "is disconnect.");
                e.printStackTrace();
                response.value = "Error sending message to " + user;
                //Save message to send and initial sender to confirm message process.
                PendingResponse pendingResponse = new PendingResponseSequential();
                pendingResponse.setResponse(message);

                PendingResponseManager.getInstance().addPendingResponse(user, pendingResponse);

            }
        }

        return response;
    }
}
