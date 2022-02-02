package bg.sofia.uni.fmi.mjt.crypto.wallet.user;

import bg.sofia.uni.fmi.mjt.crypto.wallet.dto.Asset;
import bg.sofia.uni.fmi.mjt.crypto.wallet.exceptions.InsufficientFundsException;
import bg.sofia.uni.fmi.mjt.crypto.wallet.quote.Quote;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Wallet {
    private double currentMoneyAmount;
    private Set<Quote> quotes;
    private double walletSummary;

    public Wallet() {
        this.currentMoneyAmount = 0;
        this.quotes = new HashSet<>();
        this.walletSummary = 0;
    }

    public void deposit(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Deposit should be a positive number");
        }

        currentMoneyAmount += amount;
    }

    public double getCurrentMoneyAmount() {
        return currentMoneyAmount;
    }

    public void withdraw(double amount) throws InsufficientFundsException {
        if (amount < 0) {
            throw new IllegalArgumentException("Withdraw should be a positive number");
        }

        if (currentMoneyAmount < amount) {
            throw new InsufficientFundsException("Not enough money in wallet to withdraw " + amount);
        }

        currentMoneyAmount -= amount;
    }

    public void addQuote(Quote quote) {
        quotes.add(quote);
    }

    public void removeQuotes(Set<Quote> toRemove) {
        this.quotes.removeAll(toRemove);
    }

    public Collection<Quote> getQuotes() {
        return Collections.unmodifiableCollection(quotes);
    }

    public double getWalletSummary() {
        return walletSummary;
    }


    @Override
    public String toString() {
        return "Wallet{" +
                "currentMoneyAmount=" + currentMoneyAmount +
                ", quotes=" + quotes +
                ", walletSummary=" + walletSummary +
                '}';
    }
}
