package responses;

import Demo.CallBackPrx;
import utils.ProxiesManager;

public class PendingResponseSequential extends PendingResponse {

    private String initialSender;

    @Override
    public boolean triggerSender(CallBackPrx callback) {

        try {
            callback.reportResponse(getResponse());

            if (initialSender != null) {

                String message = "Response sent received: " + getResponse();

                try {
                    CallBackPrx callBackPrx = ProxiesManager.getInstance().getProxy(initialSender);
                    callBackPrx.reportResponse(message);
                } catch (Exception e) {
                    e.printStackTrace();

                    //If the user is not connected, we save the response to send it when the user connects

                    PendingResponse pendingResponse = new PendingResponse();
                    pendingResponse.setResponse(message);
                    PendingResponseManager.getInstance().addPendingResponse(initialSender, pendingResponse);
                
                }
            }

        } catch (Exception e) {
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
