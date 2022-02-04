package bg.sofia.uni.fmi.mjt.crypto.wallet;

import bg.sofia.uni.fmi.mjt.crypto.wallet.cache.Cache;
import bg.sofia.uni.fmi.mjt.crypto.wallet.cache.DaemonCacheUpdaterTest;
import bg.sofia.uni.fmi.mjt.crypto.wallet.exceptions.InsufficientFundsException;
import bg.sofia.uni.fmi.mjt.crypto.wallet.exceptions.UserAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.crypto.wallet.storage.UsersStorage;
import bg.sofia.uni.fmi.mjt.crypto.wallet.user.User;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.internal.matchers.Any;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
public class ClientRequestHandlerTest {

    @Mock
    private static Socket socket;

    private static UsersStorage storage = new UsersStorage();

    private static Cache cache = new Cache();

    @Mock
    private static OutputStream os;

    @Mock
    private static InputStream is;

    private static ClientRequestHandler requestHandler;

    private static ExecutorService executor = Executors.newFixedThreadPool(2);

    @BeforeEach
    public void setUpTestCase() throws IOException {

        Mockito.when(socket.getOutputStream()).thenReturn(os);
        Mockito.when(socket.getInputStream()).thenReturn(is);
        requestHandler = new ClientRequestHandler(socket, storage, cache);

    }



    @AfterAll
    public static void tearDown() {
        executor.shutdown();
    }

    @Test
    public void testRegisterAddsNewUser() throws InterruptedException {
        try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))){

            String string = "register user1 email1 pass1";
            InputStream stream = new ByteArrayInputStream(string.getBytes
                    (Charset.forName("UTF-8")));

            Mockito.when(socket.getInputStream()).thenReturn(stream);
            executor.execute(requestHandler);
            Thread.sleep(500);

            Assertions.assertFalse(storage.getUsers().isEmpty(), "New user was not added");

        } catch (IOException e) {
            Assertions.fail("a reading/writing problem occurred");
        }
    }

    @Test
    public void testLoginSuccess() throws UserAlreadyExistsException, InterruptedException {
        try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))){

            storage.addUser(User.of("{\"name\":\"userName1\",\"email\":\"userEmail1\",\"password\":\"325118261\",\"wallet\":{\"currentMoneyAmount\":100.0,\"quotes\":[],\"walletSummary\":0.0}}"));

            String string = "login userEmail1 userPass1";
            InputStream stream = new ByteArrayInputStream(string.getBytes
                    (Charset.forName("UTF-8")));

            Mockito.when(socket.getInputStream()).thenReturn(stream);

            executor.execute(requestHandler);
            Thread.sleep(1000);

            Assertions.assertTrue(requestHandler.isLogged(), "The user was not logged correctly");
        } catch (IOException e) {
            Assertions.fail("a reading/writing problem occurred");
        }
    }

    @Test
    public void testLogout() throws InterruptedException {
        try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))){

            requestHandler.setLogged(true);
            String string = "logout";
            InputStream stream = new ByteArrayInputStream(string.getBytes
                    (Charset.forName("UTF-8")));

            Mockito.when(socket.getInputStream()).thenReturn(stream);

            executor.execute(requestHandler);
            Thread.sleep(1000);

            Assertions.assertFalse(requestHandler.isLogged(), "The user was not logged out correctly");
        } catch (IOException e) {
            Assertions.fail("a reading/writing problem occurred");
        }
    }

    @Test
    public void testBuySuccess() throws UserAlreadyExistsException, InterruptedException {
        try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))){

            storage.addUser(User.of("{\"name\":\"userName2\",\"email\":\"userEmail2\",\"password\":\"325118262\",\"wallet\":{\"currentMoneyAmount\":100.0,\"quotes\":[],\"walletSummary\":0.0}}"));
            requestHandler.setLogged(true);
            requestHandler.setActiveUser(storage.getUsers().stream().filter(user->user.getEmail().equals("userEmail2")).findFirst().get());

            String string = "buy BTC 100";
            InputStream stream = new ByteArrayInputStream(string.getBytes
                    (Charset.forName("UTF-8")));

            Mockito.when(socket.getInputStream()).thenReturn(stream);

            executor.execute(requestHandler);
            Thread.sleep(1000);

           Assertions.assertFalse(requestHandler.getActiveUser().getWallet().getQuotes().isEmpty(), "The user should have quotes after buying");
        } catch (IOException e) {
            Assertions.fail("a reading/writing problem occurred");
        }
    }

    @Test
    public void testSellSuccess() throws UserAlreadyExistsException, InterruptedException {
        try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            storage.addUser(User.of("{\"name\":\"userName3\",\"email\":\"userEmail3\",\"password\":\"325118263\",\"wallet\":{\"currentMoneyAmount\":100.0,\"quotes\":[{\"amount\":1.0,\"asset\":{\"asset_id\":\"BTC\",\"name\":\"BitCoin\",\"type_is_crypto\":1,\"price_usd\":1,\"purchaseDate\":\"01-02-2022 02:17\"}}],\"walletSummary\":0.0}}"));
            requestHandler.setLogged(true);
            requestHandler.setActiveUser(storage.getUsers().stream().filter(user -> user.getEmail().equals("userEmail3")).findFirst().get());

            String string = "sell BTC";
            InputStream stream = new ByteArrayInputStream(string.getBytes
                    (Charset.forName("UTF-8")));

            Mockito.when(socket.getInputStream()).thenReturn(stream);

            executor.execute(requestHandler);
            Thread.sleep(1000);

            Assertions.assertTrue(requestHandler.getActiveUser().getWallet().getQuotes().isEmpty(), "The user shouldn't have quotes after selling");

        } catch (IOException e) {
            Assertions.fail("a reading/writing problem occurred");
        }

    }

    @Test
    public void testDepositMoneySuccess() throws UserAlreadyExistsException, InterruptedException {
        try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            storage.addUser(User.of("{\"name\":\"userName4\",\"email\":\"userEmail4\",\"password\":\"325118264\",\"wallet\":{\"currentMoneyAmount\":0.0,\"quotes\":[{\"amount\":1.0,\"asset\":{\"asset_id\":\"BTC\",\"name\":\"BitCoin\",\"type_is_crypto\":1,\"price_usd\":1,\"purchaseDate\":\"01-02-2022 02:17\"}}],\"walletSummary\":0.0}}"));
            requestHandler.setLogged(true);
            requestHandler.setActiveUser(storage.getUsers().stream().filter(user -> user.getEmail().equals("userEmail4")).findFirst().get());

            String string = "deposit-money 100";
            InputStream stream = new ByteArrayInputStream(string.getBytes
                    (Charset.forName("UTF-8")));

            Mockito.when(socket.getInputStream()).thenReturn(stream);

            executor.execute(requestHandler);
            Thread.sleep(1000);

            double expected = 100;
            double actual = requestHandler.getActiveUser().getWallet().getCurrentMoneyAmount();

            Assertions.assertEquals(expected, actual, "Deposit wasn't correct");

        } catch (IOException e) {
            Assertions.fail("a reading/writing problem occurred");
        }
    }


    @Test
    public void testWithdrawMoneySuccess() throws UserAlreadyExistsException, InterruptedException {
        try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            storage.addUser(User.of("{\"name\":\"userName5\",\"email\":\"userEmail5\",\"password\":\"325118265\",\"wallet\":{\"currentMoneyAmount\":100.0,\"quotes\":[{\"amount\":1.0,\"asset\":{\"asset_id\":\"BTC\",\"name\":\"BitCoin\",\"type_is_crypto\":1,\"price_usd\":1,\"purchaseDate\":\"01-02-2022 02:17\"}}],\"walletSummary\":0.0}}"));
            requestHandler.setLogged(true);
            requestHandler.setActiveUser(storage.getUsers().stream().filter(user -> user.getEmail().equals("userEmail5")).findFirst().get());

            String string = "withdraw-money 100";
            InputStream stream = new ByteArrayInputStream(string.getBytes
                    (Charset.forName("UTF-8")));

            Mockito.when(socket.getInputStream()).thenReturn(stream);

            executor.execute(requestHandler);
            Thread.sleep(1000);

            double expected = 0;
            double actual = requestHandler.getActiveUser().getWallet().getCurrentMoneyAmount();

            Assertions.assertEquals(expected, actual, "Withdrawal wasn't correct");

        } catch (IOException e) {
            Assertions.fail("a reading/writing problem occurred");
        }
    }


    @Disabled
    @Test
    public void testUnknownCommand() throws InterruptedException {
        try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            String string = "unknown command";
            InputStream stream = new ByteArrayInputStream(string.getBytes
                    (Charset.forName("UTF-8")));

            Mockito.when(socket.getInputStream()).thenReturn(stream);

            executor.execute(requestHandler);
            Thread.sleep(1000);

            ;

        } catch (IOException e) {
            Assertions.fail("a reading/writing problem occurred");
        }
    }
}
