package bg.sofia.uni.fmi.mjt.crypto.wallet.cache;

import bg.sofia.uni.fmi.mjt.crypto.wallet.dto.Asset;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Cache {
    private Set<Asset> cachedAssets;
    private LocalDateTime lastUpdated;

    public Cache() {
        this.cachedAssets = new HashSet<>();
        this.lastUpdated = LocalDateTime.now();
    }

    public void update(Set<Asset> updates) {
        cachedAssets.clear();
        cachedAssets = updates;
        lastUpdated = LocalDateTime.now();
    }

    public Collection<Asset> getCachedAssets() {
        return Collections.unmodifiableCollection(cachedAssets);
    }
}
