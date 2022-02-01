package bg.sofia.uni.fmi.mjt.crypto.wallet.user;

import bg.sofia.uni.fmi.mjt.crypto.wallet.dto.Asset;
import bg.sofia.uni.fmi.mjt.crypto.wallet.exceptions.InsufficientFundsException;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Wallet {
    private double currentMoneyAmount;
    private Set<Asset> assets;
    private double walletSummary;

    public Wallet() {
        this.currentMoneyAmount = 0;
        this.assets = new HashSet<>();
        this.walletSummary = 0;
    }

    public void deposit(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Deposit should be a positive number");
        }

        currentMoneyAmount += amount;
    }

    public void withdraw(double amount) throws InsufficientFundsException {
        if (amount < 0) {
            throw new IllegalArgumentException("Withdraw should be a positive number");
        }

        if (currentMoneyAmount < amount) {
            throw new InsufficientFundsException("Not enough money in wallet to withdraw " + amount);
        }

        currentMoneyAmount += amount;
    }

    public void addAsset(Asset asset) {
        assets.add(asset);
    }

    public Collection<Asset> getAssets() {
        return Collections.unmodifiableCollection(assets);
    }

    public double getWalletSummary() {
        return walletSummary;
    }
}