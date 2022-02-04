package bg.sofia.uni.fmi.mjt.crypto.wallet.cache;

import bg.sofia.uni.fmi.mjt.crypto.wallet.dto.Asset;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

public class CacheTest {

    private static Cache cache = new Cache();

    @Test
    public void testUpdate() {
        Set<Asset> update = new HashSet<>();
        update.add(new Asset("asset1", "name1", 1, new BigDecimal(1)));
        update.add(new Asset("asset2", "name2", 2, new BigDecimal(2)));

        cache.update(update);

        Assertions.assertTrue(update.containsAll(cache.getCachedAssets()), "The cache was not updated correctly");
        Assertions.assertTrue(cache.getCachedAssets().containsAll(update), "The cache was not updated correctly");
    }

}
