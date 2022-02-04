package bg.sofia.uni.fmi.mjt.crypto.wallet.storage;

import bg.sofia.uni.fmi.mjt.crypto.wallet.exceptions.UserAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.crypto.wallet.user.User;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;


import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class UsersStorage implements Storage {
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

    private Set<User> users;

    public UsersStorage() {
        this.users = new HashSet<>();
    }

    private boolean validateEmail(User user) {
        return users.stream().anyMatch(otherUser -> user.getEmail().equals(otherUser.getEmail()));
    }

    @Override
    public void addUser(User user) throws UserAlreadyExistsException {
        if (!validateEmail(user)) {
            users.add(user);
        } else {
            throw new UserAlreadyExistsException("User with email already exists");
        }
    }

    @Override
    public void loadStorage(Reader reader) {
        var bufferedReader = new BufferedReader(reader);
        String data = "";

        data += bufferedReader.lines().collect(Collectors.joining(","));
        Type type = new TypeToken<Set<User>>() { }.getType();
        users = GSON.fromJson(data, type);
    }

    @Override
    public void saveStorage(Writer writer) {
        String json = GSON.toJson(users);

        try {
            writer.write(json);
            writer.flush();
        } catch (IOException e) {
            throw new IllegalStateException("A problem occurred while writing to the database file", e);
        }
    }

    public Collection<User> getUsers() {
        return Collections.unmodifiableCollection(users);
    }
}
