import com.zeroc.Ice.Current;

public class CallbackI  implements Demo.CallBack{

    @Override
    public String reportResponse(String msg, Current current) {
        

        System.out.println("Server response: " + msg);
        return "Server response: " + msg;
    }



    
}
