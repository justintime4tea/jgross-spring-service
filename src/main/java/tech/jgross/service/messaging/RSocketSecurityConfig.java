package tech.jgross.service.messaging;

import java.util.concurrent.CancellationException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.rsocket.EnableRSocketSecurity;
import org.springframework.security.config.annotation.rsocket.RSocketSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.messaging.handler.invocation.reactive.AuthenticationPrincipalArgumentResolver;
import org.springframework.security.rsocket.core.PayloadSocketAcceptorInterceptor;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Hooks;


@Configuration
@EnableRSocketSecurity
@EnableReactiveMethodSecurity
@Slf4j
public class RSocketSecurityConfig {

  @Bean
  RSocketMessageHandler messageHandler(RSocketStrategies strategies) {
    Hooks.onErrorDropped(e -> {
      if (e instanceof CancellationException || e.getCause() instanceof CancellationException) {
        log.trace("Operator called default onErrorDropped", e);
      } else {
        log.error("Operator called default onErrorDropped", e);
      }
    });

    RSocketMessageHandler handler = new RSocketMessageHandler();
    handler.getArgumentResolverConfigurer().addCustomResolver(new AuthenticationPrincipalArgumentResolver());
    handler.setRSocketStrategies(strategies);
    return handler;
  }

  @Bean
  MapReactiveUserDetailsService authentication() {
    //This is NOT intended for production use (it is intended for getting started experience only)
    UserDetails user = User.withDefaultPasswordEncoder()
        .username("user")
        .password("pass")
        .roles("USER")
        .build();

    UserDetails admin = User.withDefaultPasswordEncoder()
        .username("test")
        .password("pass")
        .roles("NONE")
        .build();

    return new MapReactiveUserDetailsService(user, admin);
  }

  @Bean
  PayloadSocketAcceptorInterceptor authorization(RSocketSecurity security) {
    security.authorizePayload(authorize -> authorize
        .anyExchange().authenticated() // all connections, exchanges.
    ).simpleAuthentication(Customizer.withDefaults());
    return security.build();
  }
}
