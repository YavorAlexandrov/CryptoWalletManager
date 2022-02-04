package bg.sofia.uni.fmi.mjt.crypto.wallet.dto;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class Asset {

    @SerializedName("asset_id")
    private final String assetId;
    private final String name;
    @SerializedName("type_is_crypto")
    private int isCrypto;
    @SerializedName("price_usd")
    private final BigDecimal price;
    private LocalDateTime purchaseDate;
    private LocalDateTime soldDate;


    public Asset(String assetId, String name, int isCrypto, BigDecimal price, LocalDateTime purchaseDate) {
        this.assetId = assetId;
        this.name = name;
        this.isCrypto = isCrypto;
        this.price = price;
        setPurchaseDate(purchaseDate);
    }

    public Asset(String assetId, String name, int isCrypto, BigDecimal price) {
        this(assetId, name, isCrypto, price, LocalDateTime.now());
    }

    public int getIsCrypto() {
        return isCrypto;
    }

    public String getAssetId() {
        return assetId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return "Asset{" +
                "assetId='" + assetId + '\'' +
                ", name='" + name + '\'' +
                ", isCrypto=" + isCrypto +
                ", price=" + price +
                ", purchaseDate=" + purchaseDate +
                ", soldDate=" + soldDate +
                '}';
    }

    public String formattedToString() {
        return "{Asset ID: " + assetId + " ; name: " + name + " ; price: " + price + "} ! ";
    }

    public void setPurchaseDate(LocalDateTime purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Asset asset = (Asset) o;
        return isCrypto == asset.isCrypto &&
                Objects.equals(assetId, asset.assetId) &&
                Objects.equals(name, asset.name) &&
                Objects.equals(price, asset.price) &&
                Objects.equals(purchaseDate, asset.purchaseDate) &&
                Objects.equals(soldDate, asset.soldDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assetId, name, isCrypto, price, purchaseDate, soldDate);
    }
}
