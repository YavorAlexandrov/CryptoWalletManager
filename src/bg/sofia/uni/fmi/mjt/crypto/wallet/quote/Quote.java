package bg.sofia.uni.fmi.mjt.crypto.wallet.quote;

import bg.sofia.uni.fmi.mjt.crypto.wallet.dto.Asset;

public class Quote {
    private double amount;
    private Asset asset;

    public Quote(double amount, Asset asset) {
        this.amount = amount;
        this.asset = asset;
    }

}
