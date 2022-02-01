package bg.sofia.uni.fmi.mjt.crypto.wallet.storage;

import bg.sofia.uni.fmi.mjt.crypto.wallet.exceptions.UserAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.crypto.wallet.user.User;

import java.io.Reader;
import java.io.Writer;

public interface Storage {

    void addUser(User user) throws UserAlreadyExistsException;

    void loadStorage(Reader reader);

    void saveStorage(Writer writer);

}
