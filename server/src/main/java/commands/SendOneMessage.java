package commands;

import com.zeroc.Ice.ConnectionRefusedException;

import Demo.CallBackPrx;
import Demo.Response;
import responses.PendingResponse;
import responses.PendingResponseManager;
import responses.PendingResponseSequential;
import utils.ProxiesManager;

import java.util.Arrays;

public class SendOneMessage implements Command {

    @Override
    public Response execute(String username, String hostname, String[] args) {

        /**
         * ARGS FORMAT: [0] = RECEIVER, [1] = message, [2] = initial sender (Optional)
         */

        //Get if exist the proxy to send the message
        Response response = new Response();

        String sender = args.length > 2 ? args[2] : hostname;

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
                    System.out.println("size of args" + args.length );
                    System.out.println("Args: " + Arrays.toString(args));
                    System.out.println("User sender: " + sender);
                    CallBackPrx callBackPrxInitialSender = ProxiesManager.getInstance().getProxy(sender);
                    callBackPrxInitialSender.reportResponse( confirmReceived);
                    response.value = "Message sent to " + args[0] + " and confirm received.";

                    return response;
                }catch (Exception e){

                    // If intial sender is not connected.
                    System.out.println("Error sending confirm received for initial sender.");
                    e.printStackTrace();

                    PendingResponse pendingResponse = new PendingResponse();
                    pendingResponse.setResponse(confirmReceived);
                    PendingResponseManager.getInstance().addPendingResponse(sender, pendingResponse);


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
                pendingResponse.setInitialSender(sender);
                PendingResponseManager.getInstance().addPendingResponse(args[0], pendingResponse);

            }
        }

        return response;

    }

}
