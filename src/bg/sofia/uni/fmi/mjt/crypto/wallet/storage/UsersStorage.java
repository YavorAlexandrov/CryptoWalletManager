package bg.sofia.uni.fmi.mjt.crypto.wallet.storage;

import bg.sofia.uni.fmi.mjt.crypto.wallet.exceptions.UserAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.crypto.wallet.user.User;

import java.util.HashSet;
import java.util.Set;

public class UsersStorage implements Storage {

    private Set<User> users;

    public UsersStorage() {
        this.users = new HashSet<>();
    }

    @Override
    public void addUser(User user) throws UserAlreadyExistsException {

    }
}
