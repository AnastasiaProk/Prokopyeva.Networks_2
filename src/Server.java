import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by Anastasia on 14.10.16.
 */
public class Server {
    private static final int PORT = 4638;
    private static final int TIMEOUT = 5000;
    private static final int SIZE = 2048;

    private static class SpeedTime {
        long lastTime;
        long sizeByte;
        public SpeedTime(){
            lastTime = System.currentTimeMillis();
            sizeByte = 0;
        }
        public void addByte (long size){
            sizeByte += size;
        }
        public float getSpeed() {
            float speed = (sizeByte / (1024 * 1024)) / (float)((System.currentTimeMillis() - lastTime) / 1000);
            lastTime = System.currentTimeMillis();
            sizeByte = 0;
            return speed;
        }
    }

    public static void main(String[] args) throws IOException {

        //ServerSocket serverSocket = new ServerSocket(PORT, 0);

        Selector selector = Selector.open();

        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(PORT));
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        long lastTime = System.currentTimeMillis();
        ByteBuffer byteBuffer = ByteBuffer.allocate(SIZE);

        for(;;) {

            int num = selector.select(TIMEOUT);

            if(System.currentTimeMillis() - lastTime > TIMEOUT) {
                Set<SelectionKey> keys = selector.selectedKeys();
                for (SelectionKey key:keys) {
                    if(key.attachment() != null){
                        System.out.println("Client's " + ((SocketChannel) key.channel()).getRemoteAddress()
                                + " speed is " + ((SpeedTime) key.attachment()).getSpeed() + " Mb/s");
                    }
                }
                lastTime = System.currentTimeMillis();
            }


            if (num == 0) {
                continue;
            }

            Set keys = selector.selectedKeys();

            Iterator it = keys.iterator();

            while (it.hasNext()) {
                SelectionKey key = (SelectionKey)it.next();

                if ((key.readyOps() & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT) {

                    SocketChannel socketChannel = serverSocketChannel.accept();
                    socketChannel.configureBlocking(false);
                    socketChannel.register(selector, SelectionKey.OP_READ).attach(new SpeedTime());

                } else if ((key.readyOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ){

                    SocketChannel socketChannel = (SocketChannel)key.channel();
                    long size;
                    try {

                        size = socketChannel.read(byteBuffer);
                    }catch (IOException e){
                        key.cancel();
                        continue;

                    }

                    if(size > 0){
                        ((SpeedTime)key.attachment()).addByte(size);
                        byteBuffer.clear();

                    }else {

                        key.cancel();
                    }
                }
            }

            // Удаляем выбранные ключи, поскольку уже отработали с ними.
            keys.clear();
        }
    }
}

