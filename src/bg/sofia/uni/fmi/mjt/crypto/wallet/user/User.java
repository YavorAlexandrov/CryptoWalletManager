package bg.sofia.uni.fmi.mjt.crypto.wallet.user;

import com.google.gson.Gson;

public class User {
    private String name;
    private String password;
    private Wallet wallet;

    public User(String name, String password, Wallet wallet) {
        this.name = name;
        this.password = password;
        this.wallet = wallet;
    }

    public User of(String line, Gson gson) {
        return gson.fromJson(line, User.class);
    }

}
