package com.github.yueki1993.lettuce_test_PR1517.without_netty_dns_resolver;

import static com.github.yueki1993.lettuce_test_PR1517.core.Constants.NETTY_DNS_RESOLVER_INSTALLED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

import io.lettuce.core.resource.DefaultClientResources;
import io.netty.resolver.DefaultAddressResolverGroup;
import org.junit.jupiter.api.Test;


public class AddressResolverGroupTests {

  @Test
  void if_netty_dns_resolver_unavailable_then_DefaultAddressResolverGroup_should_be_used() {
    assertFalse(NETTY_DNS_RESOLVER_INSTALLED);

    assertThat(DefaultClientResources.DEFAULT_ADDRESS_RESOLVER_GROUP)
        .isInstanceOf(DefaultAddressResolverGroup.class);
  }
}
