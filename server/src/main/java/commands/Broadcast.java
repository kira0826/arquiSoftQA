package commands;

import Demo.CallBackPrx;
import Demo.Response;
import responses.PendingResponse;
import responses.PendingResponseManager;
import responses.PendingResponseSequential;
import utils.ProxiesManager;

public class Broadcast implements Command{

    @Override
    public Response execute(String username, String hostname, String[] args) {

        /**
         *
         * ARGS FORMAT: [0] = message, [1] = USER (OPTIONAL)
         *
         */


        //Get if exist the proxy to send the message
        Response response = new Response();


        String user = args.length > 1 ? args[1] : hostname;

        ProxiesManager.getInstance().getAllProxies().forEach((k,v) -> {

                    CallBackPrx callBackPrx = v;

                    if (callBackPrx != null) {

                        String message = "Message from: " + user + " | Message:" + args[0];

                        try {

                            //Send message.
                            callBackPrx.reportResponse(message);
                            String confirmReceived = "Response was received - Content:" + args[0] + " | send to: " + k + "\n ";

                            try {

                                //Report response to initial sender.

                                System.out.println("User: " + user);
                                System.out.println("---------------" +"key: " +  k   + "|  Proxie" + v  + "--------------- ");

                                CallBackPrx callBackPrxInitialSender = ProxiesManager.getInstance().getProxy(user);
                                callBackPrxInitialSender.reportResponse(confirmReceived);

                            } catch (Exception e) {

                                // If intial sender is not connected.
                                System.out.println("Error sending confirm received for initial sender.");
                                e.printStackTrace();

                                PendingResponse pendingResponse = new PendingResponse();
                                pendingResponse.setResponse(confirmReceived);
                                PendingResponseManager.getInstance().addPendingResponse(user, pendingResponse);


                            }

                        } catch (Exception e) {

                            //If the receiver is not connected.

                            System.out.println("Error sending message to " + k + " - On: SendOneMessage");
                            e.printStackTrace();

                            response.value = "Error sending message to " + k;

                            //Save message to send and initial sender to confirm message process.
                            PendingResponseSequential pendingResponse = new PendingResponseSequential();
                            pendingResponse.setResponse(message);
                            pendingResponse.setInitialSender(user);
                            PendingResponseManager.getInstance().addPendingResponse(k, pendingResponse);

                        }
                    }

                });

        response.value = "Broadcast done";
        return response;
    }
}
