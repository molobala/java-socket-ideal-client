package com.molo.socket.ideal.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Request {
    private static long count=1;
    public String query;

    public ObjectNode data;
    public long hash;
    public RequestResultCallBack callback;
    public List<MediaFile> medias=null;

    public Request(String query,ObjectNode data){
        this.data=data;
        this.query=query;
        hash=count++;
        data.put("hash",hash);
        data.put("command",query);

        //mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    public Request(String query){
        this.query=query;
        ObjectMapper mapper=new ObjectMapper();
        data=mapper.createObjectNode();
        hash=count++;
        data.put("hash",hash);
        data.put("command",query);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public String toString() {
        return data.toString();
    }
    public void addFiles(List<File> m){
        if(medias==null)
            medias= new ArrayList<>();
        for(File f:m)
        {
            MediaFile mf=new MediaFile();
            //mf.file=f;
            mf.name=f.getName();
            mf.absolutePath=f.getAbsolutePath();
            mf.size=f.length();
            mf.type="";
            medias.add(mf);
        }
    }
    public void addFile(File f){
        if(medias==null)
            medias= new ArrayList<>();
        MediaFile mf=new MediaFile();
        //mf.file=f;
        mf.name=f.getName();
        mf.absolutePath=f.getAbsolutePath();
        mf.size=f.length();
        mf.type="";
        medias.add(mf);
    }
    public void addFile(MediaFile mf){
        if(medias==null)
            medias= new ArrayList<>();
        medias.add(mf);
    }
    public void execute(){
        ObjectMapper mapper=new ObjectMapper();
        if(medias !=  null && !medias.isEmpty()){
            try {
                ArrayNode arr=data.putArray("files");
                for(MediaFile m:medias)
                    arr.add(mapper.readTree(mapper.writeValueAsString(m)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        RequestManager.pushQuery(this);
        RequestManager.execute(this);
    }
}
