package bg.sofia.uni.fmi.mjt.crypto.wallet.quote;

import bg.sofia.uni.fmi.mjt.crypto.wallet.dto.Asset;

import java.util.Objects;

public class Quote {
    private double amount;
    private Asset asset;

    public Quote(double amount, Asset asset) {
        this.amount = amount;
        this.asset = asset;
    }

    public double getAmount() {
        return amount;
    }

    public Asset getAsset() {
        return asset;
    }

    @Override
    public String toString() {
        return "Quote{" +
                "amount=" + amount +
                ", asset=" + asset +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Quote quote = (Quote) o;
        return Double.compare(quote.amount, amount) == 0 && Objects.equals(asset, quote.asset);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, asset);
    }
}
