package experiment.os;

import experiment.os.block.DataBlocks;
import experiment.os.block.SuperBlock;
import experiment.os.block.base.Block;
import experiment.os.block.base.Directory;
import experiment.os.block.base.DirectoryItem;
import experiment.os.block.base.DiskINode;
import experiment.os.command_parser.CommandParser;
import experiment.os.exception.BlockNotEnough;
import experiment.os.exception.DirectoryIsFull;
import experiment.os.myEnum.FileType;
import experiment.os.myEnum.MessageType;
import experiment.os.properties.GlobalProperties;
import experiment.os.system.*;
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

    private static Server serverInstance;

    private Server() {}

    public static Server getInstance() {
        if (serverInstance == null) {
            serverInstance = new Server();
        }
        return serverInstance;
    }

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
                String result = CommandParser.parse(message, session.getCurrentPathArray(), session);
                channel.write(jointMessage(MessageType.NORMAL, session, result));
            } else if (messageType.equals(MessageType.LOGOUT)) {
                Session logoutSession = sessionMap.remove(channel.socket().getPort());
                if (logoutSession != null)
                    logoutSession.getUser().logout();
                channel.write(jointMessage(MessageType.EXIT, "bye!"));
                channel.close();
                return;

            } else if (messageType.equals(MessageType.EXIT)) {

                for (Session detachSession : sessionMap.values()) {
                    detachSession.getUser().logout();
                }

                // 缓存的写回
                BlockBuffer.getInstance().clear();
                // disk 写回
                MemSuperBlock.getInstance().save();
                BFD.getInstance().save();
                DataBlocks.getInstance().save();
                UserManager.save();
                // 关闭程序
                System.exit(0);
            }

            channel.register(selector, SelectionKey.OP_READ);
        } catch (Exception e) {
            channel.close();
            // force user logout
            Session logoutSession = sessionMap.remove(channel.socket().getPort());
            if (logoutSession != null)
                logoutSession.getUser().logout();

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
                String result = CommandParser.parse(message, session.getCurrentPathArray(), session);
                channel.write(jointMessage(MessageType.NORMAL, session, result));
            } catch (Exception e) {
                e.printStackTrace();
                channel.write(jointMessage(MessageType.NORMAL, session, ""));
            }
        }
    }

    private void login(String message, SocketChannel channel) throws IOException {
        // parse message : name + password
        String[] s = message.split(" ");
        if (s.length < 2) {
            channel.write(jointMessage(MessageType.INITIAL, "Login error, retry!"));
            return;
        }

        String name = s[0];
        String pwd = s[1];
        User loginUser = UserManager.login(name, pwd);
        if (loginUser == null) {
            channel.write(jointMessage(MessageType.INITIAL, "Login error, retry!"));
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
            channel.write(jointMessage(MessageType.INITIAL, "Register error, retry!"));
            return;
        }

        // check enough block and inode
        if (!MemSuperBlock.getInstance().hasFreeBlock(1) ||
            !BFD.getInstance().hasFreeInode(1)) {
            channel.write(jointMessage(MessageType.INITIAL, "Register error, retry!"));
            return;
        }

        String name = s[0];
        String pwd = s[1];
        User registerUser = UserManager.register(name, pwd);
        if (registerUser == null) {
            channel.write(jointMessage(MessageType.INITIAL, "Register error, retry!"));
            return;
        }

        // allocate inode and directory
        try {
            int[] freeBlockIndex = MemSuperBlock.getInstance().dispaterBlock(1);
            BlockBuffer.getInstance().set(freeBlockIndex[0], new Directory());
            Integer freeINodeIndex = BFD.getInstance().getFreeINodeIndex();
            DiskINode diskINode = BFD.getInstance().get(freeINodeIndex);
            diskINode.initInode(registerUser.getDefaultFolderMode(), (short) freeBlockIndex[0], FileType.DIRECTORY, registerUser.getUid(), registerUser.getGid());

            // add item in MFD
            DiskINode MFD = BFD.getInstance().get(BFD.MFD_INDEX);
            BlockBufferItem MFDBBI = BlockBuffer.getInstance().get(MFD.getFirstBlock());
            MFDBBI.setModified(true);
            Directory MFDir = (Directory) MFDBBI.getBlock();
            MFDir.addItem(new DirectoryItem(name.toCharArray(), freeINodeIndex));
        } catch (BlockNotEnough blockNotEnough) {
            // 不可能发生
            blockNotEnough.printStackTrace();
        } catch (DirectoryIsFull directoryIsFull) {
            // TODO 避免
            directoryIsFull.printStackTrace();
        }

        channel.write(jointMessage(MessageType.INITIAL, "Register Success!\nPlease login or register!"));
    }

    public static void main(String[] args) throws IOException {
        Server.getInstance().run();
    }
}
