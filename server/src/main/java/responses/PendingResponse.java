package responses;

import Demo.CallBackPrx;
import interfaces.ITriggerSender;

public class PendingResponse implements ITriggerSender {



    private String response;

    @Override
    public boolean triggerSender(CallBackPrx callback) {

        try {
            callback.reportResponse(response);
        } catch (Exception e) {
            return false;
        }
        return true;

    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }


    
}
