package com.github.yueki1993.lettuce_test_PR1517.with_netty_dns_resolver;

import static com.github.yueki1993.lettuce_test_PR1517.core.Constants.NETTY_DNS_RESOLVER_INSTALLED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.lettuce.core.resource.DefaultClientResources;
import io.netty.resolver.dns.DnsAddressResolverGroup;
import org.junit.jupiter.api.Test;


public class AddressResolverGroupTests {

  @Test
  void if_netty_dns_resolver_available_then_dns_address_resolver_group_should_be_used()
      throws Exception {
    assertTrue(NETTY_DNS_RESOLVER_INSTALLED);

    assertThat(DefaultClientResources.DEFAULT_ADDRESS_RESOLVER_GROUP)
        .isInstanceOf(DnsAddressResolverGroup.class);
  }
}
