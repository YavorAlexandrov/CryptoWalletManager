package bg.sofia.uni.fmi.mjt.crypto.wallet;

import bg.sofia.uni.fmi.mjt.crypto.wallet.cache.Cache;
import bg.sofia.uni.fmi.mjt.crypto.wallet.cache.DaemonCacheUpdater;
import bg.sofia.uni.fmi.mjt.crypto.wallet.storage.UsersStorage;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class CryptoWalletServer {
    private static UsersStorage usersStorage = new UsersStorage();
    private static Cache assetsCache = new Cache();
    private static final int SERVER_PORT = 4444;
    private static final int MAX_EXECUTOR_THREADS = 10;

    public static void main(String[] args) {
        Executor executor = Executors.newFixedThreadPool(MAX_EXECUTOR_THREADS);
        DaemonCacheUpdater cacheUpdater = new DaemonCacheUpdater(assetsCache);
        cacheUpdater.setDaemon(true);
        cacheUpdater.start();

        //usersStorage.loadStorage();

        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            System.out.println("Server started and listening for connect requests");

            Socket clientSocket;

            while(true) {

                clientSocket = serverSocket.accept();

                System.out.println("Accepted connection request from client " + clientSocket.getInetAddress());

                ClientRequestHandler clientHandler = new ClientRequestHandler(clientSocket, usersStorage, assetsCache);

                executor.execute(clientHandler);
            }

        } catch (IOException e) {
            throw new RuntimeException("There is a problem with the server socket", e);
        } finally {
            //usersStorage.saveStorage();
        }


    }
}
