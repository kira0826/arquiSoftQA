import com.zeroc.Ice.Current;

import java.util.ArrayList;
import java.util.List;

public class CallbackI  implements Demo.CallBack{



    static private int counter = 0 ;

    static private List<Long> currentTimes = new ArrayList<>();

    @Override
    public String reportResponse(String msg, Current current) {
        

        System.out.println("Server response: " + msg);

        counter++;
        currentTimes.add(System.currentTimeMillis());
        return "Server response: " + msg;
    }


    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public List<Long> getCurrentTimes() {
        return currentTimes;
    }

    public void setCurrentTimes(List<Long> currentTimes) {
        this.currentTimes = currentTimes;
    }
}
