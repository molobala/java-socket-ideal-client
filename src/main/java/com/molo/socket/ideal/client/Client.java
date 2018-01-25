package com.molo.socket.ideal.client;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


public class Client extends RequestHandler{
    private Socket socket;
    private ObjectMapper mapper=new ObjectMapper();
    private DataOutputStream mDout;
    private DataInputStream mDin;
    private Thread readThread;
    private MessageListener listener;
    public String HOST;
    public int PORT;
    volatile boolean run=true;
    private static final int MAX_REQUEST=100;
    private Thread writeThread;
    private final BlockingQueue<String> requestQueue=new BlockingQueue<>(MAX_REQUEST);
    public Client(){
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    public void open(String host,int port) throws IOException {
        this.PORT=port;
        this.HOST=host;
        this.socket=new Socket(host,port);
        mDin=new DataInputStream(socket.getInputStream());
        mDout=new DataOutputStream(socket.getOutputStream());
        readThread=new ReadThread();
        writeThread=new Thread(()->{
            while(run){
                try {
                    String s=requestQueue.take();
                    //Log.e("WriteThread ",s);
                    sendMessage(s);
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        writeThread.start();
    }
    public void start(){
        if(readThread.getState()== Thread.State.NEW)
            readThread.start();
    }
    public void stop() throws IOException {
        close();
    }
    public void close() throws IOException {
        run=false;
        if(mDout!=null) this.mDout.close();
        if(mDin!=null) this.mDin.close();
        if(socket!=null) this.socket.close();
        if(writeThread.isAlive()) writeThread.interrupt();
        if(readThread.isAlive())
            readThread.interrupt();
    }
    public void sendMessage(String message){
        try {
            if(mDout!=null) this.mDout.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setListener(MessageListener listener) {
        this.listener = listener;
    }

    class ReadThread extends Thread{
        @Override
        public void run() {
            run=true;
            while (run){
                try {
                    //we read data from mDin  and parse it and dispatch the message to up component
                    String line=mDin.readUTF();
                    if(line==null) break;
                    JsonNode result=mapper.readTree(line);
                    if(!result.has("hash") || !result.has("status"))
                        continue;
                    //Log.e("CLient.reading",result.toString());
                    Response r=new Response(result.get("hash").asLong(),result.get("status").asInt(), result.get("data"));
                    MediaFile[] medias=null;
                    List<MediaFile> meds=new ArrayList<>();
                    if(result.has("files")){
                        medias= mapper.readValue(result.get("files").toString(),MediaFile[].class);
                    }
                    r.medias=medias;
                    if (!dispatchResponse(r)) {
                        if(listener!=null && r.hash<=0)
                            listener.onReceiveNewMessage(r);
                    }
                    Thread.sleep(100);
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
            try {
                close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void send(String s) {
        /*new Thread(()->{
            sendMessage(s);
        }).start();*/
        try {
            requestQueue.put(s);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public DataOutputStream getOutputStream() {
        return mDout;
    }

    @Override
    public DataInputStream getInputStream() {
        return mDin;
    }

    /*@Override
    public void sendMediaFile(MediaFile file) {
        MFtp.ftpPut(file.absolutePath,getOutputStream());
    }*/

    public static class BlockingQueue<T> {
        private Queue<T> queue = new LinkedList<T>();
        private int capacity;

        public BlockingQueue(int capacity) {
            this.capacity = capacity;
        }

        public synchronized void put(T element) throws InterruptedException {
            while(queue.size() == capacity) {
                wait();
            }

            queue.add(element);
            notify(); // notifyAll() for multiple producer/consumer threads
        }

        public synchronized T take() throws InterruptedException {
            while(queue.isEmpty()) {
                wait();
            }

            T item = queue.remove();
            notify(); // notifyAll() for multiple producer/consumer threads
            return item;
        }
    }

}
