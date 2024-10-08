package responses;

import java.util.Iterator;
import java.util.Queue;

import Demo.CallBackPrx;

public class UpdateMessagesJob implements Runnable {

    private Queue<PendingResponse> queue;

    private CallBackPrx callback;

    public UpdateMessagesJob(Queue<PendingResponse> queue, CallBackPrx callback) {

        this.callback = callback;
        this.queue = queue;
    }

    @Override
    public void run() {


        Iterator<PendingResponse> iterator = queue.iterator();
        while (iterator.hasNext()) {
            PendingResponse pendingResponse = iterator.next();
            if (pendingResponse.triggerSender(callback)) {
                iterator.remove();


                System.out.println("Response was deleted: " + pendingResponse);
            } else {
                System.out.println("Do not delete the pending response.");
            }
        }
    }
}
