package com.dart.api.infrastructure.redis;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisConfiguration;
import org.springframework.stereotype.Component;

import com.dart.global.config.RedisConfig;

import lombok.RequiredArgsConstructor;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

@Component
public class RedisKeyPatternDeleter {

	@Value("${spring.data.redis.host}")
	private String redisHost;

	@Value("${spring.data.redis.port}")
	private int redisPort;

	private static final int SCAN_COUNT = 100;
	private static final String INITIAL_CURSOR = "0";


	public void deleteKeysByPatterns(List<String> patterns) {
		try (Jedis jedis = new Jedis(redisHost, redisPort)) {
			for (String pattern : patterns) {
				deleteKeysByPattern(jedis, pattern);
			}
		}
	}

	private void deleteKeysByPattern(Jedis jedis, String pattern) {
		ScanParams scanParams = new ScanParams().match(pattern).count(SCAN_COUNT);
		String cursor = INITIAL_CURSOR;
		Set<String> keys = new HashSet<>();

		do {
			ScanResult<String> scanResult = jedis.scan(cursor, scanParams);
			keys.addAll(scanResult.getResult());
			cursor = scanResult.getCursor();
		} while (!INITIAL_CURSOR.equals(cursor));

		if (!keys.isEmpty()) {
			jedis.del(keys.toArray(new String[0]));
		}
	}
}
