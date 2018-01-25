package com.molo.socket.ideal.client;

public class Response {
    public Response(long hash,int status, Object d) {
        // TODO Auto-generated constructor stub
        this.hash=hash;
        this.data=d;
        this.status=status;
    }
    public long hash;
    public int status;
    public Object data;
    public MediaFile[] medias;
}
