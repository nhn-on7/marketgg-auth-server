package com.nhnacademy.marketgg.auth.config;

import com.nhnacademy.marketgg.auth.exception.SecureManagerException;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * Redis 기본 설정을 담당합니다.
 *
 * @author 윤동열, 이제훈
 * @version 1.0.0
 */
@Configuration
public class RedisConfig {

    private final RestTemplate restTemplate;
    private final String host;
    private final int port;
    private final int database;
    private final String password;

    /**
     * RedisConfig 생성자입니다.
     *
     * @param restTemplate     - Redis 명령어를 수행하기 위한 고수준 추상화 클래스
     * @param redisInfoUrl     - 암호화한 Redis Info 경로
     * @param redisPasswordUrl - 암호화한 Redis 비밀번호 경로
     */
    public RedisConfig(@Qualifier("clientCertificateAuthenticationRestTemplate") RestTemplate restTemplate,
                       @Value("${gg.redis.url}") String redisInfoUrl,
                       @Value("${gg.redis.password-url}") String redisPasswordUrl) {

        this.restTemplate = restTemplate;
        String[] info = this.getRedisInfo(redisInfoUrl);
        this.host = info[0];
        this.port = Integer.parseInt(info[1]);
        this.database = Integer.parseInt(info[2]);
        this.password = this.getRedisPassword(redisPasswordUrl);
    }

    /**
     * Redis 연결과 관련된 설정을 하는 RedisConnectionFactory 를 스프링 빈으로 등록한다.
     * key-value 형 데이터베이스를 사용하여 프로젝트를 데이터베이스에 연결하도록 지원하는 팩토리다.
     *
     * @return Thread-safe 한 Lettuce 기반의 커넥션 팩토리 (LettuceConnectionFactory)
     * @author 윤동열
     * @see <a href="https://lettuce.io/core/release/api">Lettuce 6.x Documentation</a>
     * @since 1.0.0
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(host, port);

        configuration.setPassword(password);
        configuration.setDatabase(database);

        return new LettuceConnectionFactory(configuration);
    }

    /**
     * Redis 서버에 명령어를 수행하기 위한 높은 수준의 추상화를 제공하는 클래스인 RedisTemplate 을 스프링 빈으로 등록합니다.
     *
     * @param redisConnectionFactory - 스프링 빈으로 등록된 RedisConnectionFactory
     * @return key-value 구조의 RedisTemplate
     * @author 윤동열
     * @see RedisConfig#redisConnectionFactory
     * @since 1.0.0
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();

        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(String.class));

        return redisTemplate;
    }

    private String[] getRedisInfo(String infoUrl) {
        ResponseEntity<Map<String, Map<String, String>>> exchange =
                restTemplate.exchange(infoUrl, HttpMethod.GET, null, new ParameterizedTypeReference<>() {
                });

        String connectInfo = Optional.ofNullable(exchange.getBody())
                                     .orElseThrow(IllegalArgumentException::new)
                                     .get("body")
                                     .get("secret");

        String[] info = connectInfo.split(":");

        if (info.length != 3) {
            throw new SecureManagerException();
        }

        return info;
    }

    private String getRedisPassword(String passwordUrl) {
        ResponseEntity<Map<String, Map<String, String>>> exchange =
                restTemplate.exchange(passwordUrl, HttpMethod.GET, null, new ParameterizedTypeReference<>() {
                });

        return Optional.ofNullable(exchange.getBody())
                       .orElseThrow(IllegalArgumentException::new)
                       .get("body")
                       .get("secret");
    }

}
