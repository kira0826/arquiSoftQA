package responses;

import Demo.CallBackPrx;
import com.zeroc.Ice.ConnectionRefusedException;
import utils.ProxiesManager;

public class PendingResponseSequential extends PendingResponse {

    private String initialSender;

    @Override
    public boolean triggerSender(CallBackPrx callback) {

        try {
            callback.reportResponse(getResponse());

            String message = "Response was received - Content: " + getResponse();

            try {
                //Send received alert to inital sender.
                CallBackPrx callBackPrx = ProxiesManager.getInstance().getProxy(initialSender);
                callBackPrx.reportResponse(message);
                System.out.println("message send to initial sender");

            } catch (ConnectionRefusedException e) {
                e.printStackTrace();
                System.out.println("User is not connected, the message will be saved.");

                //If the initial sender user is not connected, we save the response to send it when the user connects

                PendingResponse pendingResponse = new PendingResponse();
                pendingResponse.setResponse(message);
                PendingResponseManager.getInstance().addPendingResponse(initialSender, pendingResponse);
                return true;
            } catch (Exception e) {
                System.out.println("Error sending response to initial sender");
                return false;
            }

        } catch (Exception e) {

            e.printStackTrace();
            return false;
        }
        return true;
    }

    public String getInitialSender() {
        return initialSender;
    }

    public void setInitialSender(String initialSender) {
        this.initialSender = initialSender;
    }

}
