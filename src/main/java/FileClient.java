import java.io.*;
import java.net.Socket;

public class FileClient {
    private static DataOutputStream dataOutputStream = null;
    private static DataInputStream dataInputStream = null;

    public static void main(String[] args)
    {
        // Create Client Socket connect to port 900
        try (Socket socket = new Socket("localhost", 900)) {

            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            System.out.println("Sending the File to the Server");
            // Call SendFile Method
            sendFile("C:\\Users\\user\\Desktop\\base", "test.txt");

            dataInputStream.close();
            dataInputStream.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void sendFile(String directory, String path)
    {
        int bytes = 0;
        // Open the File where he located in your pc

        File folder = new File(directory);
        if(!folder.exists()){
            folder.mkdirs();
            System.out.println("init directory -> "+folder.getPath());
        }
        else {
            System.out.println("Starting from ->"+folder.getPath());
        }

        File file = new File(folder.getPath(), path);
        try{
            FileInputStream fileInputStream = new FileInputStream(file);

            // Here we send the File to Server
            dataOutputStream.writeLong(file.length());
            // Here we  break file into chunks
            byte[] buffer = new byte[4 * 1024];
            while ((bytes = fileInputStream.read(buffer))
                    != -1) {
                // Send the file to Server Socket
                dataOutputStream.write(buffer, 0, bytes);
                dataOutputStream.flush();
            }
            // close the file here
            fileInputStream.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}