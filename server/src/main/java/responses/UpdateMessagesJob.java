package responses;

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

        queue.forEach((pendingResponse) -> {
            if (pendingResponse.triggerSender(callback)) {
                queue.remove(pendingResponse);
            }
        });
    }
}
