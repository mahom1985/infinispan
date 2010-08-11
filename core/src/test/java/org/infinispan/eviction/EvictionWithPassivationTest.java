package org.infinispan.eviction;

import org.infinispan.Cache;
import org.infinispan.config.Configuration;
import org.infinispan.loaders.CacheStoreConfig;
import org.infinispan.loaders.dummy.DummyInMemoryCacheStore;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.test.SingleCacheManagerTest;
import org.testng.annotations.Test;


@Test(groups = "functional", testName = "eviction.EvictionWithPassivationTest")
public class EvictionWithPassivationTest extends SingleCacheManagerTest {

   private Configuration buildCfg(EvictionThreadPolicy threadPolicy, EvictionStrategy strategy) {
      Configuration cfg = new Configuration();
      CacheStoreConfig cacheStoreConfig = new DummyInMemoryCacheStore.Cfg();
      cacheStoreConfig.setPurgeOnStartup(true);
      cfg.getCacheLoaderManagerConfig().addCacheLoaderConfig(cacheStoreConfig);
      cfg.getCacheLoaderManagerConfig().setPassivation(true);
      cfg.setEvictionStrategy(strategy);
      cfg.setEvictionThreadPolicy(threadPolicy);
      cfg.setEvictionMaxEntries(1);
      return cfg;
   }

   @Override
   protected EmbeddedCacheManager createCacheManager() throws Exception {
      cacheManager = new DefaultCacheManager();

      for (EvictionStrategy s : EvictionStrategy.values()) {
         for (EvictionThreadPolicy p : EvictionThreadPolicy.values()) {
            cacheManager.defineConfiguration("test-" + p + "-" + s, buildCfg(p, s));
         }
      }

      return cacheManager;
   }

   public void testPiggybackLRU() {
      runTest(EvictionThreadPolicy.PIGGYBACK, EvictionStrategy.LRU);
   }

   public void testPiggybackLIRS() {
      runTest(EvictionThreadPolicy.PIGGYBACK, EvictionStrategy.LIRS);
   }

   public void testPiggybackNONE() {
      runTest(EvictionThreadPolicy.PIGGYBACK, EvictionStrategy.NONE);
   }

   public void testPiggybackUNORDERED() {
      runTest(EvictionThreadPolicy.PIGGYBACK, EvictionStrategy.UNORDERED);
   }

   public void testDefaultLRU() {
      runTest(EvictionThreadPolicy.DEFAULT, EvictionStrategy.LRU);
   }

   public void testDefaultLIRS() {
      runTest(EvictionThreadPolicy.DEFAULT, EvictionStrategy.LIRS);
   }

   public void testDefaultNONE() {
      runTest(EvictionThreadPolicy.DEFAULT, EvictionStrategy.NONE);
   }

   public void testDefaultUNORDERED() {
      runTest(EvictionThreadPolicy.DEFAULT, EvictionStrategy.UNORDERED);
   }

   private void runTest(EvictionThreadPolicy p, EvictionStrategy s) {
      String name = "test-" + p + "-" + s;
      Cache<String, String> testCache = cacheManager.getCache(name);
      testCache.clear();
      testCache.put("X", "4567");
      testCache.put("Y", "4567");
      testCache.put("Z", "4567");

      assert null != testCache.get("X") : "Failure on test " + name;
      assert null != testCache.get("Y") : "Failure on test " + name;
      assert null != testCache.get("Z") : "Failure on test " + name;
   }

}