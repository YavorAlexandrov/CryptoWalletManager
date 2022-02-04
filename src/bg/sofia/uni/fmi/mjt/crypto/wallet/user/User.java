package bg.sofia.uni.fmi.mjt.crypto.wallet.user;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class User {
    private String name;
    private String email;
    private String password;
    private Wallet wallet;

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
                private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

                @Override
        public JsonElement serialize(LocalDateTime localDateTime, Type srcType, JsonSerializationContext context) {
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

    public User(String name, String email, String password, Wallet wallet) {
        this.name = name;
        this.email = email;
        this.password = String.valueOf(password.hashCode());
        this.wallet = wallet;
    }

    public static User of(String line) {
        User user = GSON.fromJson(line, User.class);
        return user;
    }

    public Wallet getWallet() {
        return wallet;
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

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(name, user.name) &&
                Objects.equals(email, user.email) &&
                Objects.equals(password, user.password) &&
                wallet.equals(user.wallet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, email, password, wallet);
    }

    public String getPassword() {
        return password;
    }
}
