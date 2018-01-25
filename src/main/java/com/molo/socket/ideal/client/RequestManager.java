package com.molo.socket.ideal.client;


import com.fasterxml.jackson.databind.ObjectMapper;
import sun.rmi.runtime.Log;


import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Molobala on 29/03/2017.
 */

public class RequestManager {
    private static WeakReference<RequestHandler> handler;
    private static WeakReference<ResponseHandler> responseHandler;
    private static WeakReference<MediaFileHandler> mediaFileHandler;
    private static ObjectMapper mapper=new ObjectMapper();
    private static final List<Request> request=new ArrayList<>();

    public static void configure(RequestHandler handler,ResponseHandler responseHandler,MediaFileHandler mediaFileHandler){
        RequestManager.handler= new WeakReference<>(handler);
        RequestManager.responseHandler=new WeakReference<>(responseHandler);
        RequestManager.mediaFileHandler=new WeakReference<>(mediaFileHandler);
        //handler.registerManager(this);
        //Request.configure(this);
    }
    public static RequestHandler getHandler(){
        return handler.get();
    }

    public static void dispose(){
        request.clear();
    }
    public static void onResult(Response r) {
        synchronized (request){
            for(Request req:request){
                if(r.hash==req.hash){
                    //you should not define 1 as a message Type
                    if(r.status==1){
                        //the server ask for media files
                        if(req.data.has("files")){
                            try {
                                MediaFile[] files=mapper.readValue(req.data.get("files").toString(),MediaFile[].class);
                                for(MediaFile m:files){
                                   if(mediaFileHandler!=null) mediaFileHandler.get().send(handler.get().getOutputStream(),m);
                                }
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                        return;
                    }
                    request.remove(req);
                    if(r.medias!=null){
                        List<MediaFile> meds=new ArrayList<>();
                        //Log.e("CLient.reading","Has Media : "+medias.length);
                        for(MediaFile m:r.medias){

                            File f= null;
                            if(mediaFileHandler!=null) {
                                f=mediaFileHandler.get().receive(handler.get().getInputStream(),m);
                            }
                            if(f!=null && !f.exists()) {
                                //f=MFtp.ftpGetFile(mDin, Config.TEMP_DIR);
                                m.absolutePath=f.getAbsolutePath();
                            }
                            //Log.e("CLient.reading","File downloaded"+f.getName());
                            meds.add(m);
                        }
                        if(!meds.isEmpty()){
                            r.medias=new MediaFile[meds.size()];
                            r.medias=meds.toArray(r.medias);

                        }else r.medias=null;
                    }
                    //q.onCallBack(r);
                    if(RequestManager.responseHandler!=null) RequestManager.responseHandler.get().onResponse(req.callback,r);
                    break;
                }
            }
        }
    }

    static void pushQuery(Request req) {
        synchronized (request){
            request.add(req);
        }
    }

    static void execute(Request req) {
        handler.get().send(req.toString());
    }
}
