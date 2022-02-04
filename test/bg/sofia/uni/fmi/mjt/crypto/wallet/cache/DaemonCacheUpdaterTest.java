package bg.sofia.uni.fmi.mjt.crypto.wallet.cache;

import bg.sofia.uni.fmi.mjt.crypto.wallet.dto.Asset;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ExtendWith(MockitoExtension.class)
public class DaemonCacheUpdaterTest {

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

    @Mock
    private static HttpClient httpClientMock;

    @Mock
    private static Cache cacheMock;

    @Mock
    private static HttpResponse<String> responseMock;

    private static DaemonCacheUpdater updater = new DaemonCacheUpdater(cacheMock);

    private static ExecutorService executor;

    @BeforeEach
    public void setUp() {
        executor = Executors.newSingleThreadExecutor();
    }

    @AfterEach
    public void tearDown() {
        executor.shutdown();
    }

    @Disabled
    @Test
    public void testUpdateCache() throws IOException, InterruptedException {
        Mockito.when(httpClientMock.send(Mockito.any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any())).thenReturn(responseMock);
        Mockito.when(responseMock.statusCode()).thenReturn(HttpURLConnection.HTTP_OK);
        Mockito.when(responseMock.body()).thenReturn("[]");
        executor.execute(updater);

        Thread.sleep(1000);

        Mockito.verify(cacheMock, Mockito.times(1)).update(Mockito.anySet());
    }


}
