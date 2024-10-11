
import java.util.Queue;

import com.zeroc.Ice.Current;

import Demo.CallBackPrx;
import Demo.Response;
import responses.PendingResponse;
import responses.PendingResponseManager;
import responses.PendingResponseSequential;
import responses.UpdateMessagesJob;
import utils.ProxiesManager;

public class ChatHandler implements Demo.ChatHandler {

    @Override
    public Response broadcast(String hostname, String message, Current current) {

        Response response = new Response();

        ProxiesManager.getInstance().getAllProxies().forEach((k, v) -> {

            CallBackPrx callBackPrx = v;

            if (callBackPrx != null) {

                String messageResponse = "Message from: " + hostname + " | Message:";
                response.value = messageResponse;

                try {

                    //Send message.
                    callBackPrx.reportResponse(response);

                    String confirmReceived = "Response was received - Content:" + message + " | send to: " + k + "\n ";

                    try {

                        CallBackPrx callBackPrxInitialSender = ProxiesManager.getInstance().getProxy(hostname);
                        response.value = confirmReceived;
                        callBackPrxInitialSender.reportResponse(response);

                    } catch (Exception e) {

                        // If intial sender is not connected.
                        System.out.println("Error sending confirm received for initial sender.");
                        e.printStackTrace();

                        PendingResponse pendingResponse = new PendingResponse();
                        pendingResponse.setResponseMessage(confirmReceived);
                        PendingResponseManager.getInstance().addPendingResponse(hostname, pendingResponse);

                    }

                } catch (Exception e) {

                    //If the receiver is not connected.
                    System.out.println("Error sending message to " + k + " - On: SendOneMessage");
                    e.printStackTrace();

                    response.value = "Error sending message to " + k;

                    //Save message to send and initial sender to confirm message process.
                    PendingResponseSequential pendingResponse = new PendingResponseSequential();
                    pendingResponse.setResponseMessage(messageResponse);
                    pendingResponse.setInitialSender(hostname);
                    PendingResponseManager.getInstance().addPendingResponse(k, pendingResponse);

                }
            }

        });

        response.value = "Broadcast done";
        return response;
    }

    @Override
    public Response listClients(String hostname, Current current) {

        Response response = new Response();

        CallBackPrx callBackPrx;

        callBackPrx = ProxiesManager.getInstance().getProxy(hostname);

        if (callBackPrx != null) {

            String message = ProxiesManager.getInstance().getAllProxies().toString();

            try {

                response.value = message;
                callBackPrx.reportResponse(response);

                response.value = "List clients process sent ";

            } catch (Exception e) {

                //If the receiver is not connected.
                System.out.println("Error sending message," + hostname + "is disconnect.");
                e.printStackTrace();
                response.value = "Error sending message to " + hostname;
                //Save message to send and initial sender to confirm message process.
                PendingResponse pendingResponse = new PendingResponseSequential();
                pendingResponse.setResponseMessage(message);

                PendingResponseManager.getInstance().addPendingResponse(hostname, pendingResponse);

            }
        }

        return response;

    }

    @Override
    public Response registerCallback(String hostname, CallBackPrx callBack, Current current) {

        Response response = new Response();

        ProxiesManager.getInstance().addProxy(hostname, callBack);

        System.out.println("Total proxies registered:");
        ProxiesManager.getInstance().getAllProxies().forEach((k, v) -> {
            System.out.println("Hostname: " + k + " | Proxy: " + v);
        });

        //Verify if there are pending responses for the client, to do that check pending queue
        Queue<PendingResponse> pendingResponses = PendingResponseManager.getInstance().getPendingResponses(hostname);
        if (pendingResponses != null && !pendingResponses.isEmpty()) {

            //If there are pending responses, we execute that job on a thread-pool
            UpdateMessagesJob updateMessagesJob = new UpdateMessagesJob(pendingResponses, callBack);

            //Possible unncessary threadpool.
            //accumulatedMessagesProcess.execute(updateMessagesJob);
        }
        return response;
    }

    @Override
    public Response sendOneMessage(String hostname, String receiver, String message, Current current) {

        /**
         * ARGS FORMAT: [0] = RECEIVER, [1] = message, [2] = initial sender
         * (Optional)
         */
        //Get if exist the proxy to send the message
        Response response = new Response();

        CallBackPrx callBackPrx = ProxiesManager.getInstance().getProxy(receiver);

        if (callBackPrx != null) {

            String messageResponse = "Message from: " + hostname + " | Message:" + receiver;

            try {
                response.value = messageResponse;
                //Send message.
                callBackPrx.reportResponse(response);

                String confirmReceived = "Response was received - Content:" + message + " | send to: " + receiver + "\n ";

                try {

                    //Report response to initial sender.
                    CallBackPrx callBackPrxInitialSender = ProxiesManager.getInstance().getProxy(hostname);
                    response.value = confirmReceived;
                    callBackPrxInitialSender.reportResponse(response);

                    return response;

                } catch (Exception e) {

                    // If intial sender is not connected.
                    System.out.println("Error sending confirm received for initial sender.");
                    e.printStackTrace();

                    PendingResponse pendingResponse = new PendingResponse();
                    pendingResponse.setResponseMessage(confirmReceived);
                    PendingResponseManager.getInstance().addPendingResponse(hostname, pendingResponse);

                    response.value = "Message sent to " + receiver + ", but do not confirm received yet.";

                    return response;
                }

            } catch (Exception e) {

                //If the receiver is not connected.
                System.out.println("Error sending message to " + receiver + " - On: SendOneMessage");
                e.printStackTrace();

                response.value = "Error sending message to " + receiver;

                //Save message to send and initial sender to confirm message process.
                PendingResponseSequential pendingResponse = new PendingResponseSequential();
                pendingResponse.setResponseMessage(messageResponse);
                pendingResponse.setInitialSender(hostname);
                PendingResponseManager.getInstance().addPendingResponse(receiver, pendingResponse);

            }
        }

        return response;

    }

}
