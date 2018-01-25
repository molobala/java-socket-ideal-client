# java-socket-ideal-client
This is an ideal java socket client class 
It contains a socket client class and a RequestManager

## Classes and they description
### Request class
This class represents a request to send to the server, requests are made by the client and are managed by a RequestManager Object  
To each request is assigned a hash code (`hash field) , which becomes usefull when a response is received from the server. 

### Response class
That class represents a response from the server, response are created by the Client from the json string recived from the server and are delivered to the RequestManager in case of request response, or to the message listener in case of server messages. 
To each response is assigned a  status code and a hash code, the request and it's response will have the same hash code, so the request manager will know wich callback to call.  
> Free are you to defined your status code in a class, generaly to will share that class between your server and the client app, because they need the same codes.  
### RequestResultCallBack interface
This interface represents a call back on a request response.
### RequestHanlder class
that is an abstract class and has 3 abstract methods:
  * `send(String s)`: should be implemented (it is implemented by the Client class) by the class that communicated by the server
  * `getOutputStream()`: should be implemented by the class that communicated by the server (here by the Client class), it returns the OutputStream underlying the connected socket to the server
  * `getInputStream()`: should be implemented by the class that communicated by the server (here by the Client class), it returns the InputStream underlying the connected socket to the server  

### ResponseHanlder interface
Thats is an interface and has just one method:
  * `onResponse(RequestResultCallBack callBack,Response response)` : should be implemented.

### MediaFileHandler interface
That interface is responsible of handling ftp operation like sending or receiving a media file.  
It contains just 2 method :
  * send(OutputStream out,MediaFile file): permits sending a media file
  * receive(InputStream in,String path): permits to receive a media file from the server
You need to implements those two methods according to how your ftp server works

### RequestManager class
This class is responsibe of request management, all request to server pass bay it, and it store them in a list, so on request result it try to find the corresponding request and call its callback if any. That means this class need a RequestHandler object and a ResponseHandle to work.  

### Client class
This is the main class that represent a socket client, it's a bit complex, but very simple to understand  
It inherits from the RequestHandler , so it can handle request and send them to the server.  
It contains two main threads:
  * `readThread`: in the loop of that thread we listen to the server and and treats so each incomming message
    Each message is readand translated to a json object  
    ```java
      String line=mDin.readUTF();
      if(line==null) break;
      JsonNode result=mapper.readTree(line);
    ```
    > **You will note that each response from the server should be a json and may have  hash and status fields necessarily, else it's ignored** 
    ```java
    if(!result.has("hash") || !result.has("status"))
      continue;
    ```
    A Response Object is then created and dispatched to the Request Manager or to the Message Listener according to the value of the hash field in the response, when this value is greater than 0 so that means the client has sent the message passing by the request manager and may have saved a callback on the request, so we send the response to the request manager, else that means we juste receive a message from the server without making any request, we transmit so the response to the listener if any!  
    * `writeThread`: that thread is responsible for queued request string and send them to the server. it use the inner class named BlockingQueue, so if there is no request, it goes in sleep.  

### MessageListener interface
That interface is listener to server message, by server message I mean , messages send by the server without any request. Those message has the `hash` fiels equals `0`.  

### MediaFile class
This class represents a media file to send or receive to/from the server. It just gives a description a file `name`,`size`,`absolutePath`,`type`. Each request can contains many MediaFile, when executing the request those media files are sent to the server. When a request with media files is sent, the server may ask the media files , if possible by sending a response with status equals to 1, so the RequestManager will not remove the request from request queue, but will send the media files associated to that request.

## Creating a client and request manager configuration
### Creating a client
To instanciate a client you juste need the `new` operator and call the `open` method to open connection to your server
```java
  client=new Client();
  client.setListener(new ClientMessageListener());
```
And of course we need to configure our RequestManager, since it's a pure static class (contains only static methods), to do that we just new to call the `configure` method of RequestManager class like this
```java
  RequestManager.configure(client, new ResponseHandler() {
      @Override
      public void onResponse(RequestResultCallBack callBack, Response response) {
          //runOnUiThread(() -> callBack.onSuccess(response)); //if the callback is created  in an android activity for example
          callBack.onSuccess(response)//this will fail if the callback is created  in an android activity and the callback change something about the views
      }
  }, new MediaFileHandler() {
      @Override
      public void send(OutputStream out, MediaFile file) {
          MFtp.ftpPut(file.absolutePath, (DataOutputStream) out);
      }

      @Override
      public File receive(InputStream in, String storePath) {
          return MFtp.ftpGetFile((DataInputStream) in, storePath);
      }
  });
```
