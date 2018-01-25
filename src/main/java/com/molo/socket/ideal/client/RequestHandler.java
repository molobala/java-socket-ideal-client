package com.molo.socket.ideal.client;


import java.io.InputStream;
import java.io.OutputStream;


public abstract class RequestHandler {
    boolean dispatchResponse(Response r){
        if(r.hash>0){
            RequestManager.onResult(r);
            return true;
        }
        return false;
    }
    public abstract void send(String s);

    public abstract OutputStream getOutputStream();
    public abstract InputStream getInputStream();
}
