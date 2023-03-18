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
                    new ServerSocketThread(socket, writerMap).start();
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

class ServerSocketThread extends Thread{
    String userId;
    InetAddress ipAddress;
    Socket socket = null;
    HashMap<String, PrintWriter> writerMap = null;
    PrintWriter output;
    BufferedReader input;
    public ServerSocketThread(
            Socket socket,
            HashMap<String, PrintWriter> writerMap
    ){
        this.socket = socket;
        this.writerMap = writerMap;
        try{
            input = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream());
            userId  = input.readLine();
            ipAddress = socket.getInetAddress();
            System.out.println(this.userId+""+this.ipAddress);
            synchronized (writerMap){
                writerMap.put(userId, output);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
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
                else{
                    // 전체 메시지
                    receiveMessage = userId + ">>>" + receiveMessage;
                    System.out.println(receiveMessage);
                    this.broadMessage(receiveMessage);
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
}