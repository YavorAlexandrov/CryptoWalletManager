package bg.sofia.uni.fmi.mjt.crypto.wallet.user;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class User {
    private String name;
    private String email;
    private String password;
    private Wallet wallet;

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

    public User(String name, String email, String password, Wallet wallet) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.wallet = wallet;
    }

    public static User of(String line) {
        User user = GSON.fromJson(line, User.class);
        return user;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", wallet=" + wallet +
                '}';
    }
}
