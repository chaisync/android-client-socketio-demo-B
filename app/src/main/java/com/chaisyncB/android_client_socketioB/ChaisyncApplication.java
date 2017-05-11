/**
 * Created by mark on 4/23/2017.
 */
package com.chaisyncB.android_client_socketioB;

import android.app.Application;
import android.util.Log;

import java.net.URISyntaxException;
import io.socket.client.IO;
import io.socket.client.Socket;

public class ChaisyncApplication extends Application{

    private Socket socket;
    {
        try {
            socket = IO.socket(Constants.SERVER_ADDRESS);
            Log.d("CSdebug", "connected to " + Constants.SERVER_ADDRESS);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public Socket getSocket(){
        return socket;
    }

}
