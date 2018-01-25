package com.molo.socket.ideal.client;

public interface RequestResultCallBack {
    void onSuccess(Response r);
    void onFail(Response r);
}
