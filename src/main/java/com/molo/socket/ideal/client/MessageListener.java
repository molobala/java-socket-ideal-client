package com.molo.socket.ideal.client;

public interface MessageListener {
    void onReceiveNewMessage(Response message);
}
