package bg.sofia.uni.fmi.mjt.crypto.wallet.user;

import bg.sofia.uni.fmi.mjt.crypto.wallet.exceptions.InsufficientFundsException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class WalletTest {

    private Wallet wallet = new Wallet();

    @Test
    public void testDepositSuccess() {
        wallet.deposit(1);
        double expected = 1;
        double actual = wallet.getCurrentMoneyAmount();
        Assertions.assertEquals(expected, actual, "The deposit didn't go through correctly");
    }

    @Test
    public void testDepositNegative() {
        Assertions.assertThrows(IllegalArgumentException.class, ()->wallet.deposit(-1), "Exception should be thrown when a negative number is passed");
    }


    @Test
    public void testWithdrawSuccess() throws InsufficientFundsException {
        wallet.deposit(1);
        wallet.withdraw(1);
        double expected = 0;
        double actual = wallet.getCurrentMoneyAmount();
        Assertions.assertEquals(expected, actual, "The withdrawal didn't go through correctly");
    }

    @Test
    public void testWithdrawNegative() {
        Assertions.assertThrows(IllegalArgumentException.class, ()->wallet.withdraw(-1), "Exception should be thrown when a negative number is passed");
    }

    @Test
    public void testWithdrawInsufficientFunds() {
        Assertions.assertThrows(InsufficientFundsException.class, ()->wallet.withdraw(1), "Exception should be thrown when there's not enough funds for the withdrawal");
    }
}
