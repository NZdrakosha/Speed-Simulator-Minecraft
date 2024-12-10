package player;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;


public class ClientSocket {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 5555;


    public static void sendClintPacket(String key){
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT)){
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF(key);
            out.writeBoolean(SpeedUp.activeSphere.get(key));

            out.flush();
            Thread.sleep(100);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
