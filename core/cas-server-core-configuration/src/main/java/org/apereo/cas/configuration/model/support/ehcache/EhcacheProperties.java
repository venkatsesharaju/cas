package org.apereo.cas.configuration.model.support.ehcache;

import org.apereo.cas.configuration.model.core.util.EncryptionRandomizedSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.support.Beans;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * This is {@link EhcacheProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */

public class EhcacheProperties {
    
    private boolean synchronousWrites;
    
    private boolean loaderAsync = true;
    
    private int maxChunkSize = 5000000;
    
    private int maximumBatchSize = 100;

    private String replicationInterval = "PT10S";
    
    private boolean replicatePuts = true;

    private boolean replicateUpdatesViaCopy = true;
    
    private boolean replicateRemovals = true;

    private boolean replicateUpdates = true;

    private boolean replicatePutsViaCopy = true;
    
    private Resource configLocation = new ClassPathResource("ehcache-replicated.xml");
    
    private boolean shared;
    
    private String cacheManagerName = "ticketRegistryCacheManager";

    private int diskExpiryThreadIntervalSeconds;
    
    private boolean eternal;
    
    private int maxElementsInMemory = 10_000;
    
    private int maxElementsInCache;

    private int maxElementsOnDisk;
    
    private String memoryStoreEvictionPolicy = "LRU";

    private String persistence = "NONE";

    /**
     * Crypto settings for the registry.
     */
    @NestedConfigurationProperty
    private EncryptionRandomizedSigningJwtCryptographyProperties crypto = new EncryptionRandomizedSigningJwtCryptographyProperties();

    public EncryptionRandomizedSigningJwtCryptographyProperties getCrypto() {
        return crypto;
    }

    public void setCrypto(final EncryptionRandomizedSigningJwtCryptographyProperties crypto) {
        this.crypto = crypto;
    }
    
    public boolean isLoaderAsync() {
        return loaderAsync;
    }

    public void setLoaderAsync(final boolean loaderAsync) {
        this.loaderAsync = loaderAsync;
    }

    public int getMaxChunkSize() {
        return maxChunkSize;
    }

    public void setMaxChunkSize(final int maxChunkSize) {
        this.maxChunkSize = maxChunkSize;
    }

    public int getMaximumBatchSize() {
        return maximumBatchSize;
    }

    public void setMaximumBatchSize(final int maximumBatchSize) {
        this.maximumBatchSize = maximumBatchSize;
    }

    public long getReplicationInterval() {
        return Beans.newDuration(replicationInterval).toMillis();
    }

    public void setReplicationInterval(final String replicationInterval) {
        this.replicationInterval = replicationInterval;
    }

    public boolean isReplicatePuts() {
        return replicatePuts;
    }

    public void setReplicatePuts(final boolean replicatePuts) {
        this.replicatePuts = replicatePuts;
    }

    public boolean isReplicateUpdatesViaCopy() {
        return replicateUpdatesViaCopy;
    }

    public void setReplicateUpdatesViaCopy(final boolean replicateUpdatesViaCopy) {
        this.replicateUpdatesViaCopy = replicateUpdatesViaCopy;
    }

    public boolean isReplicateRemovals() {
        return replicateRemovals;
    }

    public void setReplicateRemovals(final boolean replicateRemovals) {
        this.replicateRemovals = replicateRemovals;
    }

    public boolean isReplicateUpdates() {
        return replicateUpdates;
    }

    public void setReplicateUpdates(final boolean replicateUpdates) {
        this.replicateUpdates = replicateUpdates;
    }

    public boolean isReplicatePutsViaCopy() {
        return replicatePutsViaCopy;
    }

    public void setReplicatePutsViaCopy(final boolean replicatePutsViaCopy) {
        this.replicatePutsViaCopy = replicatePutsViaCopy;
    }

    public Resource getConfigLocation() {
        return configLocation;
    }

    public void setConfigLocation(final Resource configLocation) {
        this.configLocation = configLocation;
    }

    public boolean isShared() {
        return shared;
    }

    public void setShared(final boolean shared) {
        this.shared = shared;
    }

    public String getCacheManagerName() {
        return cacheManagerName;
    }

    public void setCacheManagerName(final String cacheManagerName) {
        this.cacheManagerName = cacheManagerName;
    }

    public int getDiskExpiryThreadIntervalSeconds() {
        return diskExpiryThreadIntervalSeconds;
    }

    public void setDiskExpiryThreadIntervalSeconds(final int diskExpiryThreadIntervalSeconds) {
        this.diskExpiryThreadIntervalSeconds = diskExpiryThreadIntervalSeconds;
    }
    
    public boolean isEternal() {
        return eternal;
    }

    public void setEternal(final boolean eternal) {
        this.eternal = eternal;
    }

    public int getMaxElementsInMemory() {
        return maxElementsInMemory;
    }

    public void setMaxElementsInMemory(final int maxElementsInMemory) {
        this.maxElementsInMemory = maxElementsInMemory;
    }

    public int getMaxElementsOnDisk() {
        return maxElementsOnDisk;
    }

    public void setMaxElementsOnDisk(final int maxElementsOnDisk) {
        this.maxElementsOnDisk = maxElementsOnDisk;
    }

    public String getMemoryStoreEvictionPolicy() {
        return memoryStoreEvictionPolicy;
    }

    public void setMemoryStoreEvictionPolicy(final String memoryStoreEvictionPolicy) {
        this.memoryStoreEvictionPolicy = memoryStoreEvictionPolicy;
    }
    
    public int getMaxElementsInCache() {
        return maxElementsInCache;
    }

    public void setMaxElementsInCache(final int maxElementsInCache) {
        this.maxElementsInCache = maxElementsInCache;
    }

    public boolean isSynchronousWrites() {
        return synchronousWrites;
    }

    public void setSynchronousWrites(final boolean synchronousWrites) {
        this.synchronousWrites = synchronousWrites;
    }

    public String getPersistence() {
        return persistence;
    }

    public void setPersistence(final String persistence) {
        this.persistence = persistence;
    }
}


