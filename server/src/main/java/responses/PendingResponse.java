package responses;

import Demo.CallBackPrx;

public class PendingResponse implements ITriggerSender {



    private String response;

    @Override
    public boolean triggerSender(CallBackPrx callback) {

        try {
            callback.reportResponse(response);
        } catch (Exception e) {
            System.out.println("Could not send response");
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
