import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

public class Client {
    Socket chatSocket = null;

    BufferedReader scanner = null;
    BufferedReader input = null;
    PrintWriter output = null;
    String sendMessage;
    String userId;
    MessageReceiver messageReceiver = null;
    public Client(String userId){
        this.userId = userId;
        try{
            chatSocket = new Socket("localhost", 2013);
            System.out.println(chatSocket);
            scanner = new BufferedReader(new InputStreamReader(System.in));
            input = new BufferedReader(new InputStreamReader((chatSocket.getInputStream())));
            output = new PrintWriter(chatSocket.getOutputStream());

            output.println(userId);
            output.flush();

            messageReceiver = new MessageReceiver(chatSocket, input);
            messageReceiver.start();
            String bufferedMessage = "";
            while(true){
                sendMessage = scanner.readLine();
                if(sendMessage.equalsIgnoreCase("/exit")){
                    System.out.println("Client EXIT NOW!");
                    output.println(sendMessage);
                    output.flush();
                    break;
                }
                else if(sendMessage.equalsIgnoreCase("/upload")){
                    System.out.print("Input File Path -> ");
                    String filePath = scanner.readLine();
                    System.out.println("FileSender Start!");
                    FileSender fileSender = new FileSender(chatSocket, filePath);
                    fileSender.start();
                }
                else{
                    output.println(sendMessage);
                    output.flush();
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            try{
                scanner.close();
                input.close();
                output.close();
                chatSocket.close();
                System.exit(0);
            }
            catch(Exception ee){
                ee.printStackTrace();
            }
        }
    }
    public static void main(String[] args){
        Scanner sc = new Scanner(System.in);
        System.out.print("input user id: ");
        String userId = sc.next();
        new Client(userId);
    }
}
class MessageReceiver extends Thread{
    Socket socket;
    BufferedReader input;
    String inputMessage;
    public MessageReceiver(Socket socket, BufferedReader input){
        this.socket = socket;
        this.input = input;
    }
    public void run(){
        try{
            while((inputMessage=input.readLine())!=null){
                System.out.println(inputMessage);
            }
        }
        catch(SocketException e){
            System.out.println("Socket Error");
            e.printStackTrace();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally {
            try{
                input.close();
                socket.close();
            }
            catch(Exception ee){
                ee.printStackTrace();
            }
        }
    }
}

class FileSender extends Thread{
    FileInputStream fileInputStream = null;
    OutputStream outputStream = null;
    DataOutputStream dataOutputStream = null;
    String fileName = "";
    String dirPath = "C:\\Users\\user\\Desktop\\base";
    PrintWriter output = null;
    public FileSender(Socket socket, String filePath){
        System.out.println(filePath);
        try{
            File folder = new File(dirPath);
            if(!folder.exists()){
                folder.mkdirs();
                System.out.println("폴더 초기화 완료");
            }
            else{
                System.out.println(folder.getPath()+"에서 파일을 업로드");
            }
            this.fileName = filePath;
            this.outputStream = socket.getOutputStream();
            this.dataOutputStream = new DataOutputStream(outputStream);
            this.output = new PrintWriter(socket.getOutputStream(), true);

        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    public void run(){
        try{
            File file = new File(this.dirPath, this.fileName);
            if(!file.exists()){
                System.out.println("No such file! ->"+file.getPath());
            }
            else{
                int bytes=0;
                fileInputStream = new FileInputStream(file);
                byte[] buffer = new byte[4096];

                int size = (int)file.length();

                System.out.println("length! "+file.length());
                output.println("/upload?filename="+fileName+"&length="+file.length());
                while(size > 0){
                    bytes=fileInputStream.read(buffer);
                    System.out.println("(size: "+size+" | bytes: "+bytes+" )");
                    if(bytes == -1){
                        dataOutputStream.flush();
                        break;
                    }
                    else{
                        dataOutputStream.write(buffer, 0, bytes);
                        dataOutputStream.flush();
                    }
                    size -= bytes;
                }
                dataOutputStream.close();
                fileInputStream.close();
            }
            System.out.println("File Send Complete!");
        }
        catch (Exception e){
            System.out.println("File Upload Error");
            e.printStackTrace();
        }
    }
}
