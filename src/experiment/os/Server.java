package experiment.os;

import experiment.os.command_parser.CommandParser;
import experiment.os.myEnum.MessageType;
import experiment.os.properties.GlobalProperties;
import experiment.os.user.User;
import experiment.os.user.UserManager;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;

public class Server {

    private static Integer PORT = GlobalProperties.getInt("port");
    private static Selector selector;
    private static final int BUFFER_SIZE = 2048;

    private Map<Integer, Session> sessionMap = new HashMap<>();

    public void run() throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);

        ServerSocket serverSocket = serverSocketChannel.socket();
        serverSocket.bind(new InetSocketAddress("localhost", PORT));

        selector = Selector.open();

        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            selector.select(); // 阻塞接收

            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();

            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();

                iterator.remove();

                if (key.isAcceptable()) {
                    handleAccept(key);
                } else if (key.isReadable()) {
                    handleRead(key);
                }
            }
        }
    }

    private void handleAccept(SelectionKey key) throws IOException {
        System.out.println("[conn] " + key);
        ServerSocketChannel server = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = server.accept();
        socketChannel.configureBlocking(false);

//        // login or register
//        socketChannel.write(jointMessage(MessageType.INITIAL, "Please login or register\n"));
        socketChannel.register(selector, SelectionKey.OP_READ);
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        channel.configureBlocking(false);
        Session session = sessionMap.get(channel.socket().getPort());

        // 从通道读取数据到缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        try {
            channel.read(buffer);
            // 输出客户端发送过来的消息
            Map.Entry<MessageType, String> data = getMessage(buffer);
            MessageType messageType = data.getKey();
            String message = data.getValue();
            System.out.println("[FROM]" + key + ": " + message);
            if (messageType.equals(MessageType.LOGIN)) {
                login(message, channel);

            } else if (messageType.equals(MessageType.REGISTER)) {
                register(message, channel);

            } else if (messageType.equals(MessageType.NORMAL)) {
                String result = CommandParser.parse(message, session.getCurrentPathArray(), session.getUser());
                channel.write(jointMessage(MessageType.NORMAL, session, result));
            }

            channel.register(selector, SelectionKey.OP_READ);
        } catch (Exception e) {
            channel.close();
            System.out.println("[diss] " + key);
        }
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

    private ByteBuffer jointMessage(MessageType messageType, Session session, String ...strings) {
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        buffer.put(messageType.getType());
        for (String string : strings) {
            buffer.put(string.getBytes());
        }
        buffer.put(("\n" + session.getCurrentPath() + " > ").getBytes());
        buffer.flip();
        return buffer;
    }

    private Map.Entry<MessageType, String> getMessage(ByteBuffer buffer) {
        buffer.flip();
        MessageType type = MessageType.getByValue(buffer.get(0));
        String text = new String(buffer.array(), 1, buffer.limit()).trim();
        return new ImmutablePair<>(type, text);
    }

    private void excute(MessageType messageType, String message, SocketChannel channel) throws IOException {
        if (messageType.equals(MessageType.LOGIN)) {
            login(message, channel);
        } else if (messageType.equals(MessageType.REGISTER)) {
            register(message, channel);
        } else if (messageType.equals(MessageType.NORMAL)) {
            Session session = sessionMap.get(channel.socket().getPort());
            try {
                String result = CommandParser.parse(message, session.getCurrentPathArray(), session.getUser());
                channel.write(jointMessage(MessageType.NORMAL, session, result));
            } catch (Exception e) {
                e.printStackTrace();
                channel.write(jointMessage(MessageType.NORMAL, session, ""));
            }
        } else if (messageType.equals(MessageType.LOGOUT)) {
            // TODO
        }
    }

    private void login(String message, SocketChannel channel) throws IOException {
        // parse message : name + password
        String[] s = message.split(" ");
        if (s.length < 2) {
            channel.write(jointMessage(MessageType.LOGIN, "Login error, retry!"));
            return;
        }

        String name = s[0];
        String pwd = s[1];
        User loginUser = UserManager.login(name, pwd);
        if (loginUser == null) {
            channel.write(jointMessage(MessageType.LOGIN, "Login error, retry!"));
            return;
        }

        Session session = new Session(new String[] {loginUser.getName()}, loginUser);
        sessionMap.put(channel.socket().getPort(), session);
        channel.write(jointMessage(MessageType.NORMAL, session));
    }

    private void register(String message, SocketChannel channel) throws IOException {
        // parse message : name + password
        String[] s = message.split(" ");
        if (s.length < 2) {
            channel.write(jointMessage(MessageType.REGISTER, "Register error, retry!"));
            return;
        }

        String name = s[0];
        String pwd = s[1];
        User registerUser = UserManager.register(name, pwd);
        if (registerUser == null) {
            channel.write(jointMessage(MessageType.REGISTER, "Register error, retry!"));
            return;
        }

        channel.write(jointMessage(MessageType.INITIAL, "Register Success!\nPlease login or register!"));
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.run();
    }
}
