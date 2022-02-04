package bg.sofia.uni.fmi.mjt.crypto.wallet.storage;

import bg.sofia.uni.fmi.mjt.crypto.wallet.exceptions.UserAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.crypto.wallet.user.User;
import bg.sofia.uni.fmi.mjt.crypto.wallet.user.Wallet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.fail;

public class UsersStorageTest {
    private static UsersStorage storage;
    private static Set<User> users;


    @BeforeAll
    public static void setUpTestCase() throws IOException {
        Reader usersStream = initUsers();

        try (var reader = new BufferedReader(usersStream)) {
            users = reader.lines()
                    .skip(1)
                    .limit(5)
                    .map(User::of)
                    .collect(Collectors.toSet());
        }

        storage = new UsersStorage();
    }

    @Test
    public void testDatabaseLoadsCorrectly() {
        storage.loadStorage(initUsers());

        int expected = users.size() + 1;
        int actual = storage.getUsers().size();
        Assertions.assertEquals(expected, actual, "Size of expected is different than actual");
    }

    @Test
    public void testAddUserNotAlreadyPresent() {
        Wallet testWallet = new Wallet();
        User newUser = new User("testUser", "testEmail", "testPassword", testWallet);
        try {
            storage.addUser(newUser);
        } catch (UserAlreadyExistsException e) {
            Assertions.fail("The user was not already present but was not added to storage");
        }

        Assertions.assertTrue(storage.getUsers().contains(newUser));
    }

    @Test
    public void testAddUserAlreadyPresent() {
        Wallet testWallet = new Wallet();
        User newUser = new User("testUser", "testEmail", "testPassword", testWallet);
        try {
            storage.addUser(newUser);
            storage.addUser(newUser);
        }catch (UserAlreadyExistsException e) {
            return;
        }
        Assertions.fail("The user was already present but was added to storage");
    }

    @Test
    public void testSaveStorageSavesCorrectly() throws IOException {
        storage.loadStorage(initUsers());
        String expected = "[null,{\"name\":\"userName1\",\"email\":\"userEmail1\",\"password\":\"325118261\",\"wallet\":{\"currentMoneyAmount\":1.0,\"quotes\":[{\"amount\":1.0,\"asset\":{\"asset_id\":\"id1\",\"name\":\"assetName1\",\"type_is_crypto\":1,\"price_usd\":1,\"purchaseDate\":\"01-02-2022 02:17\"}}],\"walletSummary\":1.0}},{\"name\":\"userName2\",\"email\":\"userEmail2\",\"password\":\"325118262\",\"wallet\":{\"currentMoneyAmount\":2.0,\"quotes\":[{\"amount\":2.0,\"asset\":{\"asset_id\":\"id2\",\"name\":\"assetName2\",\"type_is_crypto\":1,\"price_usd\":2,\"purchaseDate\":\"02-02-2022 02:17\"}}],\"walletSummary\":2.0}},{\"name\":\"userName3\",\"email\":\"userEmail3\",\"password\":\"325118263\",\"wallet\":{\"currentMoneyAmount\":3.0,\"quotes\":[{\"amount\":3.0,\"asset\":{\"asset_id\":\"id3\",\"name\":\"assetName3\",\"type_is_crypto\":1,\"price_usd\":3,\"purchaseDate\":\"03-02-2022 02:17\"}}],\"walletSummary\":3.0}},{\"name\":\"userName4\",\"email\":\"userEmail4\",\"password\":\"325118264\",\"wallet\":{\"currentMoneyAmount\":4.0,\"quotes\":[{\"amount\":4.0,\"asset\":{\"asset_id\":\"id4\",\"name\":\"assetName4\",\"type_is_crypto\":1,\"price_usd\":4,\"purchaseDate\":\"04-02-2022 02:17\"}}],\"walletSummary\":4.0}},{\"name\":\"userName5\",\"email\":\"userEmail5\",\"password\":\"325118265\",\"wallet\":{\"currentMoneyAmount\":5.0,\"quotes\":[{\"amount\":5.0,\"asset\":{\"asset_id\":\"id5\",\"name\":\"assetName5\",\"type_is_crypto\":1,\"price_usd\":5,\"purchaseDate\":\"05-02-2022 02:17\"}}],\"walletSummary\":5.0}}]";
        String actual = "";
        try (var writer = new StringWriter()) {

            storage.saveStorage(writer);
            actual = writer.toString();
        } catch (IOException e) {
            Assertions.fail("Unexpected IOException");
        }

        Assertions.assertEquals(expected, actual, "Saved data is different than expected");
    }

    @Test
    public void testGetUsersUnmodifiable() {
        try {
            storage.getUsers().clear();
        } catch (UnsupportedOperationException e) {
            return;
        }

        fail("returned collection is not unmodifiable");
    }


    public static Reader initUsers(){
        String[] users = {
                "[",
                "{\"name\":\"userName1\",\"email\":\"userEmail1\",\"password\":\"325118261\",\"wallet\":{\"currentMoneyAmount\":1.0,\"quotes\":[{\"amount\":1.0,\"asset\":{\"asset_id\":\"id1\",\"name\":\"assetName1\",\"type_is_crypto\":1,\"price_usd\":1,\"purchaseDate\":\"01-02-2022 02:17\"}}],\"walletSummary\":1.0}}",
                "{\"name\":\"userName2\",\"email\":\"userEmail2\",\"password\":\"325118262\",\"wallet\":{\"currentMoneyAmount\":2.0,\"quotes\":[{\"amount\":2.0,\"asset\":{\"asset_id\":\"id2\",\"name\":\"assetName2\",\"type_is_crypto\":1,\"price_usd\":2,\"purchaseDate\":\"02-02-2022 02:17\"}}],\"walletSummary\":2.0}}",
                "{\"name\":\"userName3\",\"email\":\"userEmail3\",\"password\":\"325118263\",\"wallet\":{\"currentMoneyAmount\":3.0,\"quotes\":[{\"amount\":3.0,\"asset\":{\"asset_id\":\"id3\",\"name\":\"assetName3\",\"type_is_crypto\":1,\"price_usd\":3,\"purchaseDate\":\"03-02-2022 02:17\"}}],\"walletSummary\":3.0}}",
                "{\"name\":\"userName4\",\"email\":\"userEmail4\",\"password\":\"325118264\",\"wallet\":{\"currentMoneyAmount\":4.0,\"quotes\":[{\"amount\":4.0,\"asset\":{\"asset_id\":\"id4\",\"name\":\"assetName4\",\"type_is_crypto\":1,\"price_usd\":4,\"purchaseDate\":\"04-02-2022 02:17\"}}],\"walletSummary\":4.0}}",
                "{\"name\":\"userName5\",\"email\":\"userEmail5\",\"password\":\"325118265\",\"wallet\":{\"currentMoneyAmount\":5.0,\"quotes\":[{\"amount\":5.0,\"asset\":{\"asset_id\":\"id5\",\"name\":\"assetName5\",\"type_is_crypto\":1,\"price_usd\":5,\"purchaseDate\":\"05-02-2022 02:17\"}}],\"walletSummary\":5.0}}"
                , "]"
        };

        return new StringReader(Arrays.stream(users).collect(Collectors.joining(System.lineSeparator())));

    }

}
