import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by Anastasia on 14.10.16.
 */
public class Client {

    private static final int PORT = 4638;


    public static void main(String[] args) throws IOException {

        Socket socket = new Socket("localhost", PORT);

        OutputStream out = socket.getOutputStream();
        byte [] bytes = "Hello, world!".getBytes();
        for( ;!socket.isClosed(); ){

            out.write(bytes);

        }

    }

}
