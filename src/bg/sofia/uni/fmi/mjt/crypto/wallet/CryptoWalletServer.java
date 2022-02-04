package bg.sofia.uni.fmi.mjt.crypto.wallet;

import bg.sofia.uni.fmi.mjt.crypto.wallet.cache.Cache;
import bg.sofia.uni.fmi.mjt.crypto.wallet.cache.DaemonCacheUpdater;
import bg.sofia.uni.fmi.mjt.crypto.wallet.storage.UsersStorage;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class CryptoWalletServer {
    private static UsersStorage usersStorage = new UsersStorage();
    private static Cache assetsCache = new Cache();
    private static final int SERVER_PORT = 4444;
    private static final int MAX_EXECUTOR_THREADS = 10;
    private static final String FILE_NAME = "users.txt";

    private static Boolean stopped = false;

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(MAX_EXECUTOR_THREADS);

        DaemonCacheUpdater cacheUpdater = new DaemonCacheUpdater(assetsCache);
        cacheUpdater.setDaemon(true);
        cacheUpdater.start();

        Scanner scanner = new Scanner(System.in);
        ServerStopper stopServer = new ServerStopper(stopped, scanner);
        stopServer.start();
        try {
            usersStorage.loadStorage(new BufferedReader(new FileReader(FILE_NAME)));
        } catch (IOException e) {
            throw new IllegalStateException("a problem occurred while loading the users", e);
        }

        DaemonUsersSaver usersSaver = new DaemonUsersSaver(FILE_NAME, usersStorage);
        usersSaver.setDaemon(true);

        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            System.out.println("Server started and listening for connect requests");

            Socket clientSocket;

            while (!stopServer.getStopped()) {

                clientSocket = serverSocket.accept();

                System.out.println("Accepted connection request from client " + clientSocket.getInetAddress());

                ClientRequestHandler clientHandler = new ClientRequestHandler(clientSocket, usersStorage, assetsCache);

                executor.execute(clientHandler);
            }

            usersSaver.start();

        } catch (IOException e) {
            throw new RuntimeException("There is a problem with the server socket", e);
        }
        try {
            usersSaver.join();
            executor.shutdown();
        } catch (InterruptedException e) {
            throw new IllegalStateException("a thread was interrupted", e);
        }

    }
}
