package experiment.os;

import experiment.os.myEnum.MessageType;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class Client {

    private static Selector selector;
    private static Scanner scanner = new Scanner(System.in);
    private static final int BUFFER_SIZE = 2048;

    public void run() throws IOException {
        selector = Selector.open();

        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(new InetSocketAddress("localhost", 9000));

        socketChannel.register(selector, SelectionKey.OP_CONNECT);

        while (true) {
            selector.select();
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();

            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();

                if (key.isConnectable()) {
                    // login or register
                    loginOrRegister(key);
                } else if (key.isReadable()) {
                    handleRead(key);
                }
            }
        }
    }

    private void loginOrRegister(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        channel.configureBlocking(false);

        if (channel.isConnectionPending()) {
            channel.finishConnect();
        }

        System.out.println("Please login or register");

        System.out.println("[1] Login\n[2] Register");
        int i = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Username: ");
        String name = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        if (i == 1) {
            channel.write(jointMessage(MessageType.LOGIN, name, " ", password));
        } else if (i == 2) {
            channel.write(jointMessage(MessageType.REGISTER, name, " ", password));
        }
        channel.register(selector, SelectionKey.OP_READ);
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        channel.configureBlocking(false);

        if (channel.isConnectionPending()) {
            channel.finishConnect();
        }


        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        channel.read(buffer);
        Map.Entry<MessageType, String> data = null;
        try {
            data = getMessage(buffer);
        } catch (IndexOutOfBoundsException i) {
            channel.close();
            return;
        }

        MessageType messageType = data.getKey();
        System.out.print(data.getValue());

        if (messageType.equals(MessageType.LOGIN)) {
            System.out.print("Username: ");
            String name = scanner.nextLine();
            System.out.print("Password: ");
            String password = scanner.nextLine();
            channel.write(jointMessage(MessageType.LOGIN, name, " ", password));

        } else if (messageType.equals(MessageType.REGISTER)) {
            System.out.print("Username: ");
            String name = scanner.nextLine();
            System.out.print("Password: ");
            String password = scanner.nextLine();
            channel.write(jointMessage(MessageType.REGISTER, name, " ", password));

        } else if (messageType.equals(MessageType.INITIAL)) {
            loginOrRegister(key);

        } else if (messageType.equals(MessageType.NORMAL)) {
            String command = "";
            while (command.equals("")) {
                command = scanner.nextLine().trim();
            }

            if (command.split(" ")[0].equals("logout")) {
                channel.write(jointMessage(MessageType.LOGOUT));
            } else {
                channel.write(jointMessage(MessageType.NORMAL, command));
            }
        }

        channel.register(selector, SelectionKey.OP_READ);
    }

    private ByteBuffer jointMessage(MessageType messageType, String ...strings) {
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        buffer.put(messageType.getType());
        for (String string : strings) {
            buffer.put(string.getBytes());
        }
        buffer.flip();
        return buffer;
    }

    private Map.Entry<MessageType, String> getMessage(ByteBuffer buffer) throws IndexOutOfBoundsException {
        buffer.flip();
        MessageType type = MessageType.getByValue(buffer.get(0));
        String text = new String(buffer.array(), 1, buffer.limit() - 1).trim();
        return new ImmutablePair<>(type, text);
    }

    public static void main(String[] args) throws IOException {
        Client client = new Client();
        client.run();
    }
}
