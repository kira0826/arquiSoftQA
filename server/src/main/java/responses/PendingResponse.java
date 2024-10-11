package responses;

import Demo.CallBackPrx;
import Demo.Response;

public class PendingResponse implements ITriggerSender {

    private String responseMessage;

    @Override
    public boolean triggerSender(CallBackPrx callback) {

        Response response = new Response();

        response.value = responseMessage;

        try {
            callback.reportResponse(response);
        } catch (Exception e) {
            System.out.println("Could not send response");
            return false;
        }
        return true;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String response) {
        this.responseMessage = response;
    }

}
