package bg.sofia.uni.fmi.mjt.crypto.wallet.cache;

import bg.sofia.uni.fmi.mjt.crypto.wallet.dto.Asset;
import bg.sofia.uni.fmi.mjt.crypto.wallet.exceptions.CryptoHttpClientException;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.stream.Collectors;

public class DaemonCacheUpdater extends Thread {
    private static Cache cache;

    private static final String API_KEY = "CB64230A-73AA-46F4-9314-E95047562BE8";

    private static final String API_ENDPOINT_SCHEME = "http";
    private static final String API_ENDPOINT_HOST = "rest.coinapi.io";
    private static final String API_ENDPOINT_PATH = "/v1/assets";
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
    private final int WAIT_TIME_TO_UPDATE = 300_000;

    private final HttpClient cryptoHttpClient = HttpClient.newBuilder().build();


    public DaemonCacheUpdater(Cache cache) {
        this.cache = cache;
    }

    private void updateCache() throws CryptoHttpClientException {
        HttpResponse<String> response = null;

        try {
            URI uri = new URI(API_ENDPOINT_SCHEME, API_ENDPOINT_HOST, API_ENDPOINT_PATH, API_ENDPOINT_QUERY.formatted(API_KEY), null);
            HttpRequest request = HttpRequest.newBuilder().uri(uri).build();

            response = cryptoHttpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new CryptoHttpClientException("a problem occurred while retrieving the data", e);
        }

        if (response.statusCode() == HttpURLConnection.HTTP_OK) {
            Type type = new TypeToken<Set<Asset>>() {
            }.getType();
            Set<Asset> assets = GSON.fromJson(response.body(), type);
            assets = assets.stream()
                    .filter(asset -> asset.getIsCrypto() == 1)
                    .limit(MAX_RESULTS_AMOUNT).collect(Collectors.toSet());

            synchronized (cache) {
                cache.update(assets);
            }
        }
    }

    @Override
    public void run() {
        while(true) {
            try {

                try {
                    updateCache();
                    //TODO log update
                } catch (CryptoHttpClientException e) {
                    //TODO log error
                }
                Thread.sleep(WAIT_TIME_TO_UPDATE);
            } catch (InterruptedException e) {
                throw new IllegalStateException("The thread was interrupted", e);
            }
        }
    }
}
