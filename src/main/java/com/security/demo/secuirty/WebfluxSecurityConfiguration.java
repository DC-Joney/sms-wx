package com.security.demo.secuirty;

import com.security.demo.secuirty.converter.ObjectToJsonStringConverter;
import com.security.demo.secuirty.strategy.DefaultJsonStrategy;
import com.security.demo.secuirty.strategy.JsonStrategy;
import com.security.demo.secuirty.support.JsonServerAuthenticationFailureHandler;
import com.security.demo.secuirty.support.JsonServerAuthenticationSuccessHandler;
import com.security.demo.secuirty.userdetails.DelegatingReactiveUserDetailsService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.convert.converter.ConverterRegistry;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.DelegatingServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.web.cors.reactive.CorsProcessor;

import java.util.Optional;

@Log4j2

@Configuration
@EnableWebFluxSecurity
public class WebfluxSecurityConfiguration  implements ApplicationContextAware, ApplicationListener<ContextRefreshedEvent>, InitializingBean {

    private ConverterRegistry converterRegistry;

    private ApplicationContext applicationContext;

    @Override
    public void afterPropertiesSet(){
        this.converterRegistry = (ConverterRegistry) DefaultConversionService.getSharedInstance();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        Optional.of(event)
                .map(ApplicationContextEvent::getApplicationContext)
                .filter(context-> context == applicationContext)
                .flatMap(context-> Optional.ofNullable(converterRegistry))
                .ifPresent(registry-> registry.addConverter(new ObjectToJsonStringConverter()));
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public MapReactiveUserDetailsService reactiveUserDetailsService(PasswordEncoder passwordEncoder){

        UserDetails userDetails = User.withUsername("admin")
                .password("admin")
                .passwordEncoder(passwordEncoder::encode)
                .roles("ADMIN").build();

        return new MapReactiveUserDetailsService(userDetails);
    }

    @Bean
    public JsonStrategy defaultJsonStrategy(){
        return new DefaultJsonStrategy();
    }

//    @Bean
//    public CorsConfigurationSource configurationSource(){
//
//        return exchange -> {
//
//        };
//
//    }

    @Bean
    public DelegatingReactiveUserDetailsService delegatingReactiveUserDetailsService(){
        return new DelegatingReactiveUserDetailsService();
    }


    @Bean
    public ServerAuthenticationSuccessHandler serverAuthenticationSuccessHandler(){
        return new JsonServerAuthenticationSuccessHandler(defaultJsonStrategy());
    }

    @Bean
    public ServerAuthenticationSuccessHandler defaultServerAuthenticationSuccessHandler(){
        return new DelegatingServerAuthenticationSuccessHandler(successHandlers());
    }

    @Bean
    public ServerAuthenticationFailureHandler serverAuthenticationFailureHandler(){
        return new JsonServerAuthenticationFailureHandler(defaultJsonStrategy());
    }

    private ServerAuthenticationSuccessHandler[] successHandlers(){
       return BeanFactoryUtils.beansOfTypeIncludingAncestors(
                applicationContext, ServerAuthenticationSuccessHandler.class, true, false)
                .values().toArray(new ServerAuthenticationSuccessHandler[0]);
    }

//    @Bean
    public CorsProcessor corsProcessor(){
        return (configuration, exchange) -> true;
    }


    @Bean

    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http.csrf().disable()
                .requestCache()
                .and()
                .formLogin()
                .authenticationSuccessHandler(defaultServerAuthenticationSuccessHandler())
                .authenticationFailureHandler(serverAuthenticationFailureHandler())
//                .loginPage("/reactor/login")
                .and()
//                .addFilterAt(null, SecurityWebFiltersOrder.FORM_LOGIN)
                .httpBasic().disable()
        .authorizeExchange()
                .pathMatchers("/search/**")
                .permitAll()
                .anyExchange()
                .authenticated();
        return http.build();
    }

}
