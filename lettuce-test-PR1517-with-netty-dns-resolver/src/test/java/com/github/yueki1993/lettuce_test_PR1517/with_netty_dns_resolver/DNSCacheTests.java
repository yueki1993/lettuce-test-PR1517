package com.github.yueki1993.lettuce_test_PR1517.with_netty_dns_resolver;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.yueki1993.lettuce_test_PR1517.core.Constants;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisConnectionException;
import io.lettuce.core.RedisURI.Builder;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.Transports;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.resource.ClientResources;
import io.netty.channel.EventLoop;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.dns.DnsRecord;
import io.netty.resolver.dns.DefaultDnsCache;
import io.netty.resolver.dns.DnsAddressResolverGroup;
import io.netty.resolver.dns.DnsCache;
import io.netty.resolver.dns.DnsCacheEntry;
import io.netty.resolver.dns.DnsNameResolverBuilder;
import java.net.InetAddress;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DNSCacheTests {

  private SpyDnsCache spyDefaultTtlDnsCache = new SpyDnsCache(0, Integer.MAX_VALUE, 0);

  private SpyDnsCache spyNoTtlDnsCache = new SpyDnsCache(0, 0, 0);


  @BeforeEach
  void setUp() {
    assertTrue(Constants.NETTY_DNS_RESOLVER_INSTALLED);
    spyDefaultTtlDnsCache.resetCacheHitCount();
    spyNoTtlDnsCache.resetCacheHitCount();
  }

  @Test
  void dnsCacheAvailableTest_redis_client() {
    connectWithIgnoreConnectError(buildRedisClient(spyDefaultTtlDnsCache));
    connectWithIgnoreConnectError(buildRedisClient(spyDefaultTtlDnsCache));
    connectWithIgnoreConnectError(buildRedisClient(spyDefaultTtlDnsCache));

    assertThat(spyDefaultTtlDnsCache.getCacheHitCount()).isEqualTo(2);
  }

  @Test
  void dnsCacheAvailableTest_redis_client_no_cache() {
    connectWithIgnoreConnectError(buildRedisClient(spyNoTtlDnsCache));
    connectWithIgnoreConnectError(buildRedisClient(spyNoTtlDnsCache));
    connectWithIgnoreConnectError(buildRedisClient(spyNoTtlDnsCache));

    assertThat(spyNoTtlDnsCache.getCacheHitCount()).isEqualTo(0);
  }


  private void connectWithIgnoreConnectError(RedisClient client) {
    try {
      client.connect().sync();
    } catch (RedisConnectionException e) {
    }
  }

  private DnsAddressResolverGroup buildDnsAddressResolverGroup(DnsCache cache) {
    return new DnsAddressResolverGroup(
        new DnsNameResolverBuilder().channelType(Transports.datagramChannelClass())
            .socketChannelType(Transports.socketChannelClass().asSubclass(
                SocketChannel.class))
            .resolveCache(cache)
    );
  }

  private RedisClient buildRedisClient(DnsCache cache) {
    RedisClient redisClient = RedisClient.create(
        ClientResources.builder().addressResolverGroup(buildDnsAddressResolverGroup(cache))
            .build(), Builder.redis("example.com", 1234).build());
    redisClient.setOptions(ClientOptions.builder()
        .socketOptions(SocketOptions.builder()
            .connectTimeout(Duration.ofMillis(10))
            .build())
        .build());
    return redisClient;
  }

  @Test
  void dnsCacheAvailableTest_redis_cluster_client() {
    connectClusterWithIgnoreConnectError(buildRedisClusterClient(spyDefaultTtlDnsCache));
    connectClusterWithIgnoreConnectError(buildRedisClusterClient(spyDefaultTtlDnsCache));
    connectClusterWithIgnoreConnectError(buildRedisClusterClient(spyDefaultTtlDnsCache));

    assertThat(spyDefaultTtlDnsCache.getCacheHitCount()).isEqualTo(2);
  }

  private RedisClusterClient buildRedisClusterClient(DnsCache cache) {
    RedisClusterClient redisClient = RedisClusterClient.create(
        ClientResources.builder().addressResolverGroup(buildDnsAddressResolverGroup(cache))
            .build(), Builder.redis("example.com", 1234).build());
    redisClient.setOptions(ClusterClientOptions.builder()
        .socketOptions(SocketOptions.builder()
            .connectTimeout(Duration.ofMillis(10))
            .build())
        .build());
    return redisClient;
  }

  private void connectClusterWithIgnoreConnectError(RedisClusterClient client) {
    try {
      client.connect().sync();
    } catch (RedisConnectionException e) {
    }
  }

  @Test
  void dnsCacheAvailableTest_redis_sentinel_client() {
    connectSentinelWithIgnoreConnectError(buildRedisClient(spyDefaultTtlDnsCache));
    connectSentinelWithIgnoreConnectError(buildRedisClient(spyDefaultTtlDnsCache));
    connectSentinelWithIgnoreConnectError(buildRedisClient(spyDefaultTtlDnsCache));

    assertThat(spyDefaultTtlDnsCache.getCacheHitCount()).isEqualTo(2);
  }


  private void connectSentinelWithIgnoreConnectError(RedisClient client) {
    try {
      client.connectSentinel().sync();
    } catch (RedisConnectionException e) {
    }
  }

  private static class SpyDnsCache implements DnsCache {

    private final DefaultDnsCache delegate;

    private final AtomicInteger cacheHitCount = new AtomicInteger(0);

    public SpyDnsCache(int minTtl, int maxTtl, int negativeTtl) {
      delegate = new DefaultDnsCache(minTtl, maxTtl, negativeTtl);
    }

    @Override
    public void clear() {
      delegate.clear();
    }

    @Override
    public boolean clear(String hostname) {
      return delegate.clear(hostname);
    }

    @Override
    public List<? extends DnsCacheEntry> get(String hostname, DnsRecord[] additionals) {
      List<? extends DnsCacheEntry> cache = delegate.get(hostname, additionals);
      if (cache != null) {
        cacheHitCount.incrementAndGet();
      }
      return cache;
    }

    @Override
    public DnsCacheEntry cache(String hostname, DnsRecord[] additionals, InetAddress address,
        long originalTtl, EventLoop loop) {
      return delegate.cache(hostname, additionals, address, originalTtl, loop);
    }

    @Override
    public DnsCacheEntry cache(String hostname, DnsRecord[] additionals, Throwable cause,
        EventLoop loop) {
      cause.printStackTrace();
      return delegate.cache(hostname, additionals, cause, loop);
    }

    public int getCacheHitCount() {
      return cacheHitCount.intValue();
    }

    public void resetCacheHitCount() {
      cacheHitCount.set(0);
    }
  }
}
