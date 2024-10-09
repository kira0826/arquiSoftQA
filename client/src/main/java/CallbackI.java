import com.zeroc.Ice.Current;

public class CallbackI  implements Demo.CallBack{

    private int count = 0;

    @Override
    public String reportResponse(String msg, Current current) {
        
        count++;
        System.out.println("Callback received: " + msg + " | Count: " + count);
        System.out.println("Server response: " + msg);
        return "Server response: " + msg;
    }



    
}
