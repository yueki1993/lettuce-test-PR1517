package com.github.yueki1993.lettuce_test_PR1517.core;

public class Constants {

  public static final boolean NETTY_DNS_RESOLVER_INSTALLED;

  static {
    boolean nonBlockingDnsResolverAvailable;
    try {
      Class.forName("io.netty.resolver.dns.DnsNameResolver");
      nonBlockingDnsResolverAvailable = true;
    } catch (ClassNotFoundException e) {
      nonBlockingDnsResolverAvailable = false;
    }
    NETTY_DNS_RESOLVER_INSTALLED = nonBlockingDnsResolverAvailable;
    System.out.println("netty-dns-resolver installed?: " + NETTY_DNS_RESOLVER_INSTALLED);
  }
}
