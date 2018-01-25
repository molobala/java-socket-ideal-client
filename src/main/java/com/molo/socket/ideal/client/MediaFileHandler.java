package com.molo.socket.ideal.client;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

public interface MediaFileHandler {
    void send(OutputStream out, MediaFile file);
    File receive(InputStream in, MediaFile mediaFile);
}
