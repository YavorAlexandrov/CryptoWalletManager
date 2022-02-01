package bg.sofia.uni.fmi.mjt.crypto.wallet;

import bg.sofia.uni.fmi.mjt.crypto.wallet.storage.Storage;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.http.HttpClient;

public class ClientRequestHandler implements Runnable {

    private Socket socket;
    private Storage storage;

    private static final String API_KEY = "CB64230A-73AA-46F4-9314-E95047562BE8";

    private static final String API_ENDPOINT_SCHEME = "http";
    private static final String API_ENDPOINT_HOST = "";
    private static final String API_ENDPOINT_PATH = "";
    private static final Gson GSON = new Gson();

    private final HttpClient newsHttpClient = HttpClient.newBuilder().build();

    public ClientRequestHandler(Socket socket, Storage usersStorage) {
        this.socket = socket;
        this.storage = usersStorage;
    }


    @Override
    public void run() {
        try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Message received from client: " + inputLine);

                String[] words = inputLine.split(" ");
                switch (words[0]) {
                    case "help" -> System.out.println("help");
                    case "register" -> System.out.println("register");
                    case "login" -> System.out.println("login");
                    case "deposit-money" -> System.out.println("deposit-money");
                    case "list-offerings" -> System.out.println("list-offerings");
                    case "buy" -> System.out.println("buy");
                    case "sell" -> System.out.println("sell");
                    case "get-wallet-summary" -> System.out.println("get-wallet-summary");
                    case "get-wallet-overall-summary" -> System.out.println("get-wallet-overall-summary");
                    default -> System.out.println("default");
                }
            }

        } catch (IOException e) {
            //TODO log error
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
