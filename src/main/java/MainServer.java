import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.HashMap;

public class MainServer {
    ServerSocket server = null;
    Socket socket = null;
    HashMap<String, PrintWriter> writerMap = new HashMap<>();
    public MainServer(){
        try {
            server = new ServerSocket(2013);
            System.out.println("연결 대기");

            while(true){
                socket = server.accept();
                System.out.println(socket);
                if(socket != null){
                    SocketAddress targetAddress = socket.getLocalSocketAddress();
                    System.out.println("Connected " + targetAddress);
                    new ServerSocketChatThread(socket, writerMap).start();
                }
                else{
                    System.out.println("socket error fucked up!");
                }

            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        new MainServer();
    }
}

class ServerSocketChatThread extends Thread{
    String userId;
    InetAddress ipAddress;
    Socket socket = null;
    HashMap<String, PrintWriter> writerMap = null;
    PrintWriter output;
    BufferedReader input;
    DataInputStream dataInputStream = null;
    public ServerSocketChatThread(
            Socket socket,
            HashMap<String, PrintWriter> writerMap
    ){
        this.socket = socket;
        this.writerMap = writerMap;
        try{
            input = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream());
            dataInputStream = new DataInputStream(socket.getInputStream());
            userId  = input.readLine();
            ipAddress = socket.getInetAddress();
            System.out.println(this.userId+""+this.ipAddress);
            synchronized (writerMap){
                writerMap.put(userId, output);
            }
            this.welcomeMessage();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    public void welcomeMessage(){
        String message = "Welcome! " + this.userId+"("+this.ipAddress+")";
        this.broadMessage(message);
    }

    public void run(){
        String receiveMessage;
        try{
            while((receiveMessage = input.readLine()) != null){
                if(receiveMessage.equalsIgnoreCase("/exit")){
                    synchronized (this.writerMap){
                        writerMap.remove(this.userId);
                        break;
                    }
                }
                else if(receiveMessage.contains("/private@")){
                    String[] splitResult = receiveMessage.split("@", 2);
                    if(splitResult.length > 0){
                        String[] tempMessage = splitResult[1].split(":::", 2);
                        if(tempMessage.length > 0){
                            String targetUser = tempMessage[0];
                            String targetMessage = tempMessage[1];
                            this.privateMessage(targetUser, targetMessage);
                        }
                        else{
                            this.privateMessage(userId, "Message Error");
                        }
                    }
                    else{
                        this.privateMessage(userId, "Failed to Execute Private Message; username required");
                    }

                }
                else if(receiveMessage.contains("/broad")){
                    // 전체 메시지
                    receiveMessage = "[public from "+ userId +"]"  +">>>" + receiveMessage;
                    System.out.println(receiveMessage);
                    this.broadMessage(receiveMessage);
                }
                else if(receiveMessage.startsWith("/upload")){
                    HashMap<String, String> paramMaps = this.parseParams(receiveMessage);
                    String filename = paramMaps.get("filename");
                    String length = paramMaps.get("length");
                    this.receiveFile(filename, length);
                }
                else{
                    System.out.println("[ what? ]: "+receiveMessage);
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally {
            synchronized (writerMap){
                writerMap.remove(userId);
                broadMessage(userId+" Exit!");
                System.out.println("Exit >> "+userId);
                try{
                    if(socket!=null){
                        input.close();
                        output.close();
                        socket.close();
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
    public HashMap<String, String> parseParams(String message){
        HashMap<String, String> paramMap = new HashMap<String, String>();
        String[] splitParams = message.split("\\?", 2);
        if(splitParams.length > 1){
            String[] parameters = splitParams[1].split("&");
            for(String temp : parameters){
                String[] kvs = temp.split("=", 2);
                if(kvs.length>1){
                    String key = kvs[0];
                    String value = kvs[1];
                    paramMap.put(key, value);
                }
            }
        }
        return paramMap;
    }
    public void receiveFile(String filename, String length){
        try{
            String dirPath = "C:\\Users\\user\\Desktop\\uploads";
            File folder = new File(dirPath);
            if(!folder.exists()){
                folder.mkdirs();
                System.out.println("폴더 초기화 완료");
            }
            else{
                System.out.println(folder.getPath()+"에서 파일을 업로드");
            }
            File file = new File(folder.getPath(), filename);
            filename = file.getPath();

            int bytes = -1 ;
            FileOutputStream fileOutputStream = new FileOutputStream(filename);
            long size = Long.parseLong(length);

            System.out.println("@@@@@@@@@@@@@");
            System.out.println(filename+" : "+size);
            System.out.println("@@@@@@@@@@@@@");

            byte[] buffer = new byte[4096];
            while(size > 0){
                bytes = dataInputStream.read(buffer, 0, (int) Math.min(size, (int)buffer.length));
                System.out.println("(size: "+size+" | bytes: "+bytes+" )");
                if (bytes == -1){
                    fileOutputStream.flush();
                    break;
                }
                fileOutputStream.write(buffer, 0, bytes);
                fileOutputStream.flush();
                size -=bytes;
            }
            System.out.println("file receive! ");
            // fileOutputStream.flush();
            fileOutputStream.close();
        }
        catch(Exception e){
            System.out.println("receive file Error");
            e.printStackTrace();
        }

    }
    public void broadMessage(String message){
        synchronized (writerMap){
            try{
                for(PrintWriter writer: writerMap.values()){
                    writer.println(message);
                    writer.flush();
                }
            }
            catch(Exception e){
                System.out.println("broadMessage Error");
                e.printStackTrace();
            }
        }
    }
    public void privateMessage(String username, String message){
        synchronized (writerMap){
            try{
                PrintWriter targetPrinter = writerMap.get(username);
                message = "[private"+" from "+userId+"] => "+message;
                if(targetPrinter != null){
                    targetPrinter.println(message);
                    targetPrinter.flush();
                }
                else{
                    System.out.println("private Messasge Fucked UP!");
                }
            }
            catch(Exception e){
                System.out.println("privateMessage Error");
                e.printStackTrace();
            }
        }
    }
}
class ServerSocketFileThread extends Thread{

}