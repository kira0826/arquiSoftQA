module Demo
{
    class Response{
        long responseTime;
        string value;
    }
    

    interface CallBack
    {
        string reportResponse(string msg);
    }

    interface Printer
    
    {

        void registerCallback(string hostname, CallBack* callBack);
        Response printString(string s);

    }

}