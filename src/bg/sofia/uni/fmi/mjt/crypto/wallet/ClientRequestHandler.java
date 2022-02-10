package bg.sofia.uni.fmi.mjt.crypto.wallet;

import bg.sofia.uni.fmi.mjt.crypto.wallet.cache.Cache;
import bg.sofia.uni.fmi.mjt.crypto.wallet.dto.Asset;
import bg.sofia.uni.fmi.mjt.crypto.wallet.exceptions.ApiKeyException;
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
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ClientRequestHandler implements Runnable {

    private Socket socket;
    private Storage storage;
    private User activeUser;
    private Cache assetsCache;

    private boolean isLogged = false;

    private static final String API_KEY = "C68D2036-9211-4C29-B4DA-C64F454EF078";

    private static final String API_ENDPOINT_SCHEME = "http";
    private static final String API_ENDPOINT_HOST = "rest.coinapi.io";
    private static final String API_ENDPOINT_PATH = "/v1/assets";
    private static final String API_ENDPOINT_ASSET_ID_PATH = "/%s";
    private static final String API_ENDPOINT_QUERY = "apikey=%s";
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
                private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

                @Override
                public JsonElement serialize(LocalDateTime localDateTime, Type srcType,
                                             JsonSerializationContext context) {
                    return new JsonPrimitive(TIME_FORMATTER.format(localDateTime));
                }
            })
            .registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
                @Override
                public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                        throws JsonParseException {
                    return LocalDateTime.parse(json.getAsString(),
                            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
                }
            }).create();

    private static final int MAX_RESULTS_AMOUNT = 20;

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
            synchronized (storage) {
                storage.addUser(new User(name, email, password, wallet));
            }

        } catch (UserAlreadyExistsException e) {
            writer.println("This user is already registered.");
            //to do log error
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


        Set<User> results = storage.getUsers().stream()
                .filter(user->email.equals(user.getEmail()))
                .collect(Collectors.toSet());
        if (results.size() == 1) {
            boolean correctPassword = results.stream()
                    .allMatch(user->user.getPassword().equals(String.valueOf(password.hashCode())));
            if (correctPassword) {
                isLogged = true;
                activeUser = results.stream().findFirst().get();
                writer.println("Login successful. Welcome " + activeUser.getName());
                writer.flush();
                return true;
            } else {
                writer.println("Invalid password. Login unsuccessful.");
                writer.flush();
                //TO DO log error
                return false;
            }
        } else {
            writer.println("Invalid email. Login unsuccessful.");
            writer.flush();
            //TO DO log no such error
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
            return;
        }

        activeUser.getWallet().deposit(amount);
        writer.println("Deposit successful.");
        writer.flush();
    }

    private void withdrawMoney(double amount, PrintWriter writer) {
        if (!isLogged) {
            writer.println("No account logged in currently.");
            return;
        }
        try {
            activeUser.getWallet().withdraw(amount);
        } catch (InsufficientFundsException e) {
            writer.println("Insufficient funds.");
            //TO DO log error
            return;
        }
        writer.println("Withdrawal successful.");
        writer.flush();
    }

    private void help(PrintWriter writer) {
        if (!isLogged) {
            writer.print("Available commands: ; ");
            writer.print("- quit ; ");
            writer.print("- register <name> <email> <password> ; ");
            writer.println("- login <email> <password> ; ");
        } else {
            writer.print("Available commands: ; ");
            writer.print("- deposit-money <amount> ; ");
            writer.print("- withdraw-money <amount> ; ");
            writer.print("- list-offerings ; ");
            writer.print("- buy <asset_id> <money_amount> ; ");
            writer.print("- sell <asset_id> ; ");
            writer.print("- get-wallet-summary ; ");
            writer.print("- get-wallet-overall-summary ; ");
            writer.print("- quit ; ");
            writer.println("- logout");
        }
    }

    private void makeOfferingsRequest(PrintWriter writer) throws CryptoHttpClientException {
        HttpResponse<String> response = null;

        try {
            URI uri = new URI(API_ENDPOINT_SCHEME, API_ENDPOINT_HOST,
                    API_ENDPOINT_PATH, API_ENDPOINT_QUERY.formatted(API_KEY), null);
            HttpRequest request = HttpRequest.newBuilder().uri(uri).build();

            response = cryptoHttpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new CryptoHttpClientException("a problem occurred while retrieving the data", e);
        }

        if (response.statusCode() == HttpURLConnection.HTTP_OK) {
            Type type = new TypeToken<Set<Asset>>() { }.getType();
            Set<Asset> assets = GSON.fromJson(response.body(), type);

            assets.stream()
                    .filter(asset -> asset.getIsCrypto() == 1)
                    .filter(asset -> asset.getPrice() != null)
                    .limit(MAX_RESULTS_AMOUNT)
                    .forEach(asset -> writer.print(asset.formattedToString()));
            writer.println("");
            return;
        }
        switch (response.statusCode()) {
            case HttpURLConnection.HTTP_BAD_REQUEST -> throw new CryptoHttpClientException("A bad request was made");
            case HttpURLConnection.HTTP_UNAUTHORIZED -> throw new ApiKeyException("Your apikey is wrong");
            case HttpURLConnection.HTTP_FORBIDDEN ->
                    throw new ApiKeyException("Your API key doesn't have enough privileges to access this resource");
        }
        throw new CryptoHttpClientException("Unexpected response code from the crypto service");
    }

    private void listOfferings(PrintWriter writer) throws CryptoHttpClientException {
        if (!isLogged) {
            writer.println("No account logged in currently.");
            return;
        }
        final int min = 30;
        if (LocalDateTime.now().minusMinutes(min).isBefore(assetsCache.getLastUpdated())) {
            assetsCache.getCachedAssets().stream()
                    .forEach(asset -> writer.print(asset.formattedToString()));

            writer.println("");
            return;
        }

        makeOfferingsRequest(writer);
    }

    private Asset checkCache(String assetID) {
        Optional<Asset> optAsset = assetsCache.getCachedAssets().stream()
                .filter(asset -> asset.getAssetId().equals(assetID))
                .findFirst();
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
        HttpResponse<String> response;

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
            Type type = new TypeToken<List<Asset>>() { }.getType();
            List<Asset> assets = GSON.fromJson(response.body(), type);

            if (assets.isEmpty() || assets.get(0).getPrice() == null) {
                writer.println("This coin wasn't found or isn't available for purchase.");
                return;
            }

            buyAsset(money, assets.get(0), writer);
            return;
        }

        switch (response.statusCode()) {
            case HttpURLConnection.HTTP_BAD_REQUEST -> throw new CryptoHttpClientException("A bad request was made");
            case HttpURLConnection.HTTP_UNAUTHORIZED -> throw new ApiKeyException("Your apikey is wrong");
            case HttpURLConnection.HTTP_FORBIDDEN ->
                    throw new ApiKeyException("Your API key doesn't have enough privileges to access this resource");
        }
        throw new CryptoHttpClientException("Unexpected response code from the crypto service");
    }

    private void buy(double money, String assetID, PrintWriter writer) throws CryptoHttpClientException {
        if (!isLogged) {
            writer.println("No account logged in currently.");
            return;
        }
        if (assetID == null || assetID.equals("")) {
            throw new IllegalArgumentException("assetID is null or empty");
        }

        if (money > activeUser.getWallet().getCurrentMoneyAmount()) {
            writer.println("Insufficient funds! Unsuccessful purchase.");
            return;
        }
        Asset cachedAsset = checkCache(assetID);
        if (cachedAsset != null) {
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
            Type type = new TypeToken<List<Asset>>() { }.getType();
            List<Asset> assets = GSON.fromJson(response.body(), type);

            if (assets.isEmpty() || assets.get(0).getPrice() == null) {
                writer.println("This coin wasn't found or can't be sold right now.");
                return;
            }

            sellAssets(assetID, assets.get(0), writer);
            return;
        }

        switch (response.statusCode()) {
            case HttpURLConnection.HTTP_BAD_REQUEST -> throw new CryptoHttpClientException("A bad request was made");
            case HttpURLConnection.HTTP_UNAUTHORIZED -> throw new ApiKeyException("Your apikey is wrong");
            case HttpURLConnection.HTTP_FORBIDDEN ->
                    throw new ApiKeyException("Your API key doesn't have enough privileges to access this resource");
        }
        throw new CryptoHttpClientException("Unexpected response code from the crypto service");
    }

    private void sell(String assetID, PrintWriter writer) throws CryptoHttpClientException {
        if (!isLogged) {
            writer.println("No account logged in currently.");
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

    private double checkWithRequest(String assetID, Quote quote) throws CryptoHttpClientException {
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
            Type type = new TypeToken<List<Asset>>() { }.getType();
            List<Asset> assets = GSON.fromJson(response.body(), type);

            return quote.getAmount() * assets.get(0).getPrice().doubleValue();
        }

        switch (response.statusCode()) {
            case HttpURLConnection.HTTP_BAD_REQUEST -> throw new CryptoHttpClientException("A bad request was made");
            case HttpURLConnection.HTTP_UNAUTHORIZED -> throw new ApiKeyException("Your apikey is wrong");
            case HttpURLConnection.HTTP_FORBIDDEN ->
                    throw new ApiKeyException("Your API key doesn't have enough privileges to access this resource");
        }

        throw new CryptoHttpClientException("Unexpected response code from the crypto service");
    }

    private double checkValueOfAsset(String assetID, Quote quote) {
        Asset cachedAsset = checkCache(assetID);
        if (cachedAsset != null) {
            return quote.getAmount() * cachedAsset.getPrice().doubleValue();
        }

        double res = -1;
        try {
            res = checkWithRequest(assetID, quote);
        } catch (CryptoHttpClientException e) {
            System.out.println("an error occurred");
        }

        return res;
    }

    private void getSummary(PrintWriter writer) {
        if (!isLogged) {
            writer.println("No account logged in currently.");
            writer.flush();
            return;
        }

        writer.print("Money to invest: " + activeUser.getWallet().getCurrentMoneyAmount() + " ; ");
        activeUser.getWallet().getQuotes()
                .stream()
                .forEach(quote -> writer.print("Value of " + quote.getAsset().getAssetId() + ": "
                                            + checkValueOfAsset(quote.getAsset().getAssetId(), quote) + " ; "));

        writer.println("");
        writer.flush();
    }

    private double overallSummary(PrintWriter writer) {
        if (!isLogged) {
            writer.println("No account logged in currently.");
            writer.flush();
            return -1;
        }

        double currVal = 0;
        double boughtVal = 0;
        for (Quote quote : activeUser.getWallet().getQuotes()) {
            currVal += checkValueOfAsset(quote.getAsset().getAssetId(), quote);
            boughtVal += quote.getAmount() * quote.getAsset().getPrice().doubleValue();
        }
        double overall = currVal - boughtVal;
        writer.println("Overall account balance: " + overall);
        writer.flush();
        return overall;
    }

    @Override
    public void run() {
        try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            String inputLine;
            final int pos3 = 3;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Message received from client: " + inputLine);

                String[] words = inputLine.split(" ");
                switch (words[0]) {
                    case "help" : help(out);
                                  break;
                    case "quit" : out.println("Exiting...");
                                  out.flush();
                                  return;
                    case "register" : register(words[1], words[2], words[pos3], out);
                                      break;
                    case "login" : login(words[1], words[2], out);
                                   break;
                    case "logout" : logout(out);
                                    break;
                    case "deposit-money" : depositMoney(Double.valueOf(words[1]), out);
                                           break;
                    case "withdraw-money" : withdrawMoney(Double.valueOf(words[1]), out);
                                            break;
                    case "list-offerings" : try { listOfferings(out); }
                        catch (CryptoHttpClientException e) {
                            out.print("An error occurred while fetching the data");
                            out.flush(); }
                        break;
                    case "buy" : try { buy(Double.valueOf(words[2]), words[1], out); }
                        catch (CryptoHttpClientException e) {
                            out.print("An error occurred while fetching the data");
                            out.flush(); }
                        break;
                    case "sell" : try { sell(words[1], out); }
                        catch (CryptoHttpClientException e) {
                            out.print("An error occurred while fetching the data");
                            out.flush(); }
                        break;
                    case "get-wallet-summary" : getSummary(out);
                                                break;
                    case "get-wallet-overall-summary" : overallSummary(out);
                                                        break;
                    default : out.println("Unknown command. Type 'help' to see available commands.");
                              out.flush();
                              break;
                }
            }

        } catch (IOException e) {
            //TO DO log error
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isLogged() {
        return isLogged;
    }

    public void setLogged(boolean logged) {
        isLogged = logged;
    }

    public void setActiveUser(User activeUser) {
        this.activeUser = activeUser;
    }

    public User getActiveUser() {
        return activeUser;
    }
}
