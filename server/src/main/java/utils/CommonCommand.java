package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.zeroc.Ice.InputStream;

public class CommonCommand {

    public static String executeCommand(String m) throws IOException {
        String str = null, output = "";
        InputStream s;
        BufferedReader r;

        Process p = Runtime.getRuntime().exec(m);

        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        while ((str = br.readLine()) != null) {
            output += str + System.getProperty("line.separator");
        }
        br.close();
        return output;
    }

}
