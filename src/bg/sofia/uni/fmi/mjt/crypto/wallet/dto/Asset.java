package bg.sofia.uni.fmi.mjt.crypto.wallet.dto;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    public String getName() {
        return name;
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
        return "{Asset ID: " + assetId + " ; name: " + name + " ; price: " + price + "} > ";
    }

    public void setPurchaseDate(LocalDateTime purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public void setSoldDate(LocalDateTime soldDate) {
        this.soldDate = soldDate;
    }
}
