package com.dart.api.infrastructure.redis;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

@Component
public class RedisDataDeleter {

	@Value("${spring.data.redis.host}")
	private String redisHost;

	@Value("${spring.data.redis.port}")
	private int redisPort;

	public void deleteKeysByPattern(List<String> patterns) {
		try (Jedis jedis = new Jedis(redisHost, redisPort)) {
			for (String pattern : patterns) {
				ScanParams scanParams = new ScanParams().match(pattern).count(100);
				String cursor = "0";
				Set<String> keys = new HashSet<>();

				do {
					ScanResult<String> scanResult = jedis.scan(cursor, scanParams);
					keys.addAll(scanResult.getResult());
					cursor = scanResult.getCursor();
				} while (!"0".equals(cursor));

				if (!keys.isEmpty()) {
					jedis.del(keys.toArray(new String[0]));
				}
			}
		}
	}
}
