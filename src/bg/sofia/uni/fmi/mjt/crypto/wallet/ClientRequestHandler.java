package bg.sofia.uni.fmi.mjt.crypto.wallet;

import bg.sofia.uni.fmi.mjt.crypto.wallet.cache.Cache;
import bg.sofia.uni.fmi.mjt.crypto.wallet.dto.Asset;
import bg.sofia.uni.fmi.mjt.crypto.wallet.exceptions.CryptoHttpClientException;
import bg.sofia.uni.fmi.mjt.crypto.wallet.exceptions.InsufficientFundsException;
import bg.sofia.uni.fmi.mjt.crypto.wallet.exceptions.UserAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.crypto.wallet.quote.Quote;
import bg.sofia.uni.fmi.mjt.crypto.wallet.storage.Storage;
import bg.sofia.uni.fmi.mjt.crypto.wallet.user.User;
import bg.sofia.uni.fmi.mjt.crypto.wallet.user.Wallet;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ClientRequestHandler implements Runnable {

    private Socket socket;
    private Storage storage;
    private User activeUser;
    private Cache assetsCache;

    private boolean isLogged = false;

    private static final String API_KEY = "CB64230A-73AA-46F4-9314-E95047562BE8";

    private static final String API_ENDPOINT_SCHEME = "http";
    private static final String API_ENDPOINT_HOST = "rest.coinapi.io";
    private static final String API_ENDPOINT_PATH = "/v1/assets";
    private static final String API_ENDPOINT_ASSET_ID_PATH = "/%s";
    private static final String API_ENDPOINT_QUERY = "apikey=%s";
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
                private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

                @Override
                public JsonElement serialize(LocalDateTime localDateTime, Type srcType, JsonSerializationContext context) {
                    return new JsonPrimitive(formatter.format(localDateTime));
                }
            })
            .registerTypeAdapter(LocalDateTime.class, new JsonDeserializer <LocalDateTime>() {
                @Override
                public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                    return LocalDateTime.parse(json.getAsString(),
                            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
                }
            }).create();

    private final int MAX_RESULTS_AMOUNT = 20;

    private final HttpClient cryptoHttpClient = HttpClient.newBuilder().build();

    public ClientRequestHandler(Socket socket, Storage usersStorage, Cache assetsCache) {
        this.socket = socket;
        this.storage = usersStorage;
        this.assetsCache = assetsCache;
    }

    private boolean register(String name, String email, String password, PrintWriter writer) {
        if (name == null && email == null || password == null) {
            throw new IllegalArgumentException("name, email or password is invalid");
        }

        try {
            Wallet wallet = new Wallet();
            synchronized (this) {
                storage.addUser(new User(name, email, password, wallet));
            }

        } catch (UserAlreadyExistsException e) {
            writer.println("This user is already registered.");
            //TODO log error
            return false;
        }

        writer.println("Registration successful. Please log in your account.");
        writer.flush();
        return true;
    }

    private boolean login(String email, String password, PrintWriter writer) {
        if (email == null || password == null) {
            throw new IllegalArgumentException("email or password is invalid");
        }


        Set<User> results = storage.getUsers().stream().filter(user->email.equals(user.getEmail())).collect(Collectors.toSet());
        if (results.size() == 1) {
            boolean correctPassword = results.stream().allMatch(user->user.getPassword().equals(String.valueOf(password.hashCode())));
            if (correctPassword) {
                isLogged = true;
                activeUser = results.stream().findFirst().get();
                writer.println("Login successful. Welcome " + activeUser.getName());
                writer.flush();
                return true;
            } else {
                writer.println("Invalid password. Login unsuccessful.");
                writer.flush();
                //TODO log error
                return false;
            }
        } else {
            writer.println("Invalid email. Login unsuccessful.");
            writer.flush();
            //TODO sout no such user
            return false;
        }

    }

    private void logout(PrintWriter writer) {
        isLogged = false;
        activeUser = null;
        writer.println("Logging out...");
        writer.flush();
    }

    private void depositMoney(double amount, PrintWriter writer) {
        if (!isLogged) {
            writer.println("No account logged in currently.");
            writer.flush();
            return;
        }

        activeUser.getWallet().deposit(amount);
        writer.println("Deposit successful.");
        writer.flush();
    }

    private void withdrawMoney(double amount, PrintWriter writer) {
        if (!isLogged) {
            writer.println("No account logged in currently.");
            writer.flush();
            return;
        }
        try {
            activeUser.getWallet().withdraw(amount);
        } catch (InsufficientFundsException e) {
            writer.println("Insufficient funds.");
            writer.flush();
            //TODO log error
            return;
        }
        writer.println("Withdrawal successful.");
        writer.flush();
    }

    private void help(PrintWriter writer) {
        if (!isLogged) {
            writer.print("Available commands: ; ");
            writer.print("-register <name> <email> <password> ; ");
            writer.println("-login <email> <password> ; ");
            writer.flush();
        } else {
            writer.print("Available commands: ; ");
            writer.print("- deposit-money <amount> ; ");
            writer.print("- withdraw-money <amount> ; ");
            writer.print("- list-offerings ; ");
            writer.print("- buy <?> ; ");
            writer.print("- sell <?> ; ");
            writer.print("- get-wallet-summary <?> ; ");
            writer.print("- get-wallet-overall-summary <?> ; ");
            writer.println("- logout");
            writer.flush();
        }
    }

    private void makeOfferingsRequest(PrintWriter writer) throws CryptoHttpClientException {
        HttpResponse<String> response = null;

        try {
            URI uri = new URI(API_ENDPOINT_SCHEME, API_ENDPOINT_HOST, API_ENDPOINT_PATH, API_ENDPOINT_QUERY.formatted(API_KEY), null);
            HttpRequest request = HttpRequest.newBuilder().uri(uri).build();

            response = cryptoHttpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new CryptoHttpClientException("a problem occurred while retrieving the data", e);
        }

        if (response.statusCode() == HttpURLConnection.HTTP_OK) {
            Type type = new TypeToken<Set<Asset>>(){}.getType();
            Set<Asset> assets = GSON.fromJson(response.body(), type);

            assets.stream()
                    .filter(asset -> asset.getIsCrypto() == 1)
                    .limit(MAX_RESULTS_AMOUNT)
                    .forEach(asset -> writer.print(asset.formattedToString()));
            writer.println("");
            writer.flush();
            return;
        }
        //TODO api errors

        throw new CryptoHttpClientException("Unexpected response code from the crypto service");
    }

    private void listOfferings(PrintWriter writer) throws CryptoHttpClientException {
        if (!isLogged) {
            writer.println("No account logged in currently.");
            writer.flush();
            return;
        }

        if (LocalDateTime.now().minusMinutes(30).isBefore(assetsCache.getLastUpdated())) {
            assetsCache.getCachedAssets().stream()
                    .forEach(asset -> writer.print(asset.formattedToString()));

            writer.println("");
            writer.flush();
            return;
        }

        makeOfferingsRequest(writer);
    }

    private Asset checkCache(String assetID) {
        Optional<Asset> optAsset = assetsCache.getCachedAssets().stream().filter(asset -> asset.getAssetId().equals(assetID)).findFirst();
        if (!optAsset.isEmpty()) {
            return optAsset.get();
        } else {
            return null;
        }
    }

    private void buyAsset(double money, Asset asset, PrintWriter writer) {
        double amountBought = money / asset.getPrice().doubleValue();
        Quote quote = new Quote(amountBought, asset);
        try {
            activeUser.getWallet().withdraw(money);
        } catch (InsufficientFundsException e) {
            throw new IllegalStateException("insufficient funds", e);
        }
        activeUser.getWallet().addQuote(quote);
        writer.println("Purchase successful.");
        writer.flush();
        return;
    }

    private void buyWithRequest(double money, String assetID, PrintWriter writer) throws CryptoHttpClientException {
        HttpResponse<String> response = null;

        try {
            URI uri = new URI(API_ENDPOINT_SCHEME,
                    API_ENDPOINT_HOST,
                    API_ENDPOINT_PATH + API_ENDPOINT_ASSET_ID_PATH.formatted(assetID),
                    API_ENDPOINT_QUERY.formatted(API_KEY), null);
            HttpRequest request = HttpRequest.newBuilder().uri(uri).build();

            response = cryptoHttpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new CryptoHttpClientException("a problem occurred while retrieving the data", e);
        }

        if (response.statusCode() == HttpURLConnection.HTTP_OK) {
            Asset asset = GSON.fromJson(response.body().toString(), Asset.class);

            buyAsset(money, asset, writer);
            return;
        }

        //TODO api errors

        throw new CryptoHttpClientException("Unexpected response code from the crypto service");
    }

    private void buy(double money, String assetID, PrintWriter writer) throws CryptoHttpClientException {
        if (!isLogged) {
            writer.println("No account logged in currently.");
            writer.flush();
            return;
        }
        if (assetID == null || assetID.equals("")) {
            throw new IllegalArgumentException("assetID is null or empty");
        }

        if (money > activeUser.getWallet().getCurrentMoneyAmount()) {
            writer.println("Insufficient funds! Unsuccessful purchase.");
            writer.flush();
            return;
        }
        Asset cachedAsset = checkCache(assetID);
        if (cachedAsset!=null) {
            buyAsset(money, cachedAsset, writer);
            return;
        }

        buyWithRequest(money, assetID, writer);
    }

    private void sellAssets(String assetID, Asset cachedAsset, PrintWriter writer) {
        Set<Quote> toSell = activeUser.getWallet().getQuotes().stream()
                .filter(quote->quote.getAsset().getAssetId().equals(assetID))
                .collect(Collectors.toSet());

        toSell.stream()
                .forEach(quote->activeUser
                        .getWallet()
                        .deposit(cachedAsset.getPrice()
                                .multiply(new BigDecimal(quote.getAmount()))
                                .doubleValue()));

        activeUser.getWallet().removeQuotes(toSell);
        writer.println("Quotes for " + assetID + " sold successfully.");
        writer.flush();
        return;
    }

    private void sellWithRequest(String assetID, PrintWriter writer) throws CryptoHttpClientException {
        HttpResponse<String> response = null;

        try {
            URI uri = new URI(API_ENDPOINT_SCHEME,
                    API_ENDPOINT_HOST,
                    API_ENDPOINT_PATH + API_ENDPOINT_ASSET_ID_PATH.formatted(assetID),
                    API_ENDPOINT_QUERY.formatted(API_KEY), null);
            HttpRequest request = HttpRequest.newBuilder().uri(uri).build();

            response = cryptoHttpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new CryptoHttpClientException("a problem occurred while retrieving the data", e);
        }

        if (response.statusCode() == HttpURLConnection.HTTP_OK) {
            Asset asset = GSON.fromJson(response.body().toString(), Asset.class);

            sellAssets(assetID, asset, writer);
            return;
        }

        //TODO api errors

        throw new CryptoHttpClientException("Unexpected response code from the crypto service");
    }

    private void sell(String assetID, PrintWriter writer) throws CryptoHttpClientException {
        if (!isLogged) {
            writer.println("No account logged in currently.");
            writer.flush();
            return;
        }
        if (assetID == null || assetID.equals("")) {
            throw new IllegalArgumentException("assetID is null or empty");
        }

        Asset cachedAsset = checkCache(assetID);
        if (cachedAsset != null) {
            sellAssets(assetID, cachedAsset, writer);
            return;
        }

        sellWithRequest(assetID, writer);

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
                    case "help" : help(out);break;
                    case "register" : register(words[1], words[2], words[3], out);break;
                    case "login" : login(words[1], words[2], out);break;
                    case "logout" : logout(out);break;
                    case "deposit-money" : depositMoney(Double.valueOf(words[1]), out);break;
                    case "withdraw-money" : withdrawMoney(Double.valueOf(words[1]), out);break;
                    case "list-offerings" : try {listOfferings(out);} catch (CryptoHttpClientException e) {out.print("An error occurred while fetching the data"); out.flush();} break;
                    case "buy" : try {buy(Double.valueOf(words[2]), words[1], out);} catch (CryptoHttpClientException e) {out.print("An error occurred while fetching the data"); out.flush();}break;
                    case "sell" : try {sell(words[1], out);} catch (CryptoHttpClientException e) {out.print("An error occurred while fetching the data"); out.flush();};break;
                    case "get-wallet-summary" : System.out.println("get-wallet-summary"); break;
                    case "get-wallet-overall-summary" : System.out.println("get-wallet-overall-summary"); break;
                    default : out.println("Unknown command. Type 'help' to see available commands."); out.flush(); break;
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
