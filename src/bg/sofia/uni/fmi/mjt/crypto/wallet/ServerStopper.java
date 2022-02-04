package bg.sofia.uni.fmi.mjt.crypto.wallet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Reader;
import java.util.Scanner;

public class ServerStopper extends Thread {
    private boolean stopped;
    private Scanner scanner;

    public ServerStopper(boolean stopped, Scanner scanner) {
        this.stopped = stopped;
        this.scanner = scanner;
    }

    @Override
    public void run() {
        while (true) {
            String message = scanner.nextLine();
            if ("STOP_SERVER".equals(message)) {
                synchronized (new Object()) {
                    stopped = true;
                    System.out.println("Server will be stopped soon.");
                    break;
                }
            }
        }
    }

    public Boolean getStopped() {
        return stopped;
    }
}
