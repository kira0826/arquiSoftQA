import com.zeroc.Ice.Current;

import Demo.Response;

public class CallbackI  implements Demo.CallBack{

    private int count = 0;

    @Override
    public void reportResponse(Response response, Current current) {
        

        System.out.println(response.value);
        
    }

  

   


    
}
