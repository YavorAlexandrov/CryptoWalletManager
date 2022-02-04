package bg.sofia.uni.fmi.mjt.crypto.wallet.user;

import bg.sofia.uni.fmi.mjt.crypto.wallet.dto.Asset;
import bg.sofia.uni.fmi.mjt.crypto.wallet.quote.Quote;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UserTest {

    private static User user;

    @Test
    public void testOf() {
        String line = "{\"name\":\"userName1\",\"email\":\"userEmail1\",\"password\":\"325118261\",\"wallet\":{\"currentMoneyAmount\":1.0,\"quotes\":[{\"amount\":1.0,\"asset\":{\"asset_id\":\"id1\",\"name\":\"assetName1\",\"type_is_crypto\":1,\"price_usd\":1,\"purchaseDate\":\"01-02-2022 02:17\"}}],\"walletSummary\":0.0}}";
        Wallet wallet = new Wallet();
        wallet.deposit(1.0);
        Asset asset = new Asset("id1", "assetName1", 1, new BigDecimal(1.0), LocalDateTime.of(2022, 02, 01, 02, 17));
        Quote quote = new Quote(1.0, asset);
        wallet.addQuote(quote);
        User expected = new User("userName1", "userEmail1", "userPass1", wallet);

        User actual = User.of(line);
        expected.equals(actual);
        Assertions.assertTrue(expected.equals(actual), "User was not parsed correctly");

    }

}
