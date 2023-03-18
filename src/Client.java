import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    Socket socket = null;
    BufferedReader scanner = null;
    BufferedReader input = null;
    PrintWriter output = null;
    String sendMessage;
    String receiveMessage;
    String userId;
    MessageReceiver messageReceiver = null;
    public Client(String userId){
        this.userId = userId;
        try{
            socket = new Socket("localhost", 2013);
            System.out.println(socket);
            scanner = new BufferedReader(new InputStreamReader(System.in));
            input = new BufferedReader(new InputStreamReader((socket.getInputStream())));
            output = new PrintWriter(socket.getOutputStream());

            output.println(userId);
            output.flush();

            messageReceiver = new MessageReceiver(socket, input);
            messageReceiver.start();
            while(true){
                sendMessage = scanner.readLine();
                output.println(sendMessage);
                output.flush();
                if(sendMessage.equalsIgnoreCase("/exit")){
                    System.out.println("Client EXIT NOW!");
                    break;
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
                socket.close();
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