package bg.sofia.uni.fmi.mjt.crypto.wallet.dto;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDateTime;

public class Asset {

    @SerializedName("asset_id")
    private final String assetId;
    private final String name;
    @SerializedName("price_usd")
    private final double price;
    private final LocalDateTime purchaseDate;
    private LocalDateTime soldDate;


    public Asset(String assetId, String name, double price) {
        this.assetId = assetId;
        this.name = name;
        this.price = price;
        this.purchaseDate = LocalDateTime.now();
    }

    public String getAssetId() {
        return assetId;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return "Asset{" +
                "assetId='" + assetId + '\'' +
                ", name='" + name + '\'' +
                ", price=" + price +
                '}';
    }
}
