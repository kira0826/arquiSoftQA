package commands;

import com.zeroc.Ice.ConnectionRefusedException;

import Demo.CallBackPrx;
import Demo.Response;
import responses.PendingResponse;
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

            String message = "Message from: " + hostname + " | Message:" +  args[1];

            try {

                //Send message.
                callBackPrx.reportResponse(message);
                response.value = "Message sent to " + args[0];
                String confirmReceived = "Response was received - Content:" + args[1] +  " | send to: " + args[0] + "\n ";

                try {

                    //Report response to initial sender.

                    CallBackPrx callBackPrxInitialSender = ProxiesManager.getInstance().getProxy(args[2]);
                    callBackPrxInitialSender.reportResponse( confirmReceived);
                    response.value = "Message sent to " + args[0] + " and confirm received.";

                    return response;
                }catch (Exception e){

                    // If intial sender is not connected.
                    System.out.println("Error sending confirm received for initial sender.");
                    e.printStackTrace();

                    PendingResponse pendingResponse = new PendingResponse();
                    pendingResponse.setResponse(confirmReceived);
                    PendingResponseManager.getInstance().addPendingResponse(args[2], pendingResponse);


                    response.value = "Message sent to " + args[0] + ", but do not confirm received yet.";

                    return  response;
                }

            } catch (Exception e ) {

                //If the receiver is not connected.

                System.out.println("Error sending message to " + args[0] + " - On: SendOneMessage");
                e.printStackTrace();

                response.value = "Error sending message to " + args[0];

                //Save message to send and initial sender to confirm message process.
                PendingResponseSequential pendingResponse = new PendingResponseSequential();
                pendingResponse.setResponse(message);
                pendingResponse.setInitialSender(args[2]);
                PendingResponseManager.getInstance().addPendingResponse(args[0], pendingResponse);

            }
        }

        return response;

    }

}
