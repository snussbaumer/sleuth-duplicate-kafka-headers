package fr.sebnuss.sleuth.repro;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.lang3.StringUtils;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import com.github.charithe.kafka.KafkaJunitRule;

import fr.sebnuss.sleuth.repro.CheckHeadersTest.TestApplication;

/**
 * @author snussbaumer
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TestApplication.class)
@WebIntegrationTest
public class CheckHeadersTest {

    @ClassRule
    public static KafkaJunitRule kafkaRule = new KafkaJunitRule(15555, 15556);

    private static Logger log = LoggerFactory.getLogger(CheckHeadersTest.class);

    @Test
    public void testKafkaSleuthHeaders() {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> result = restTemplate.exchange("http://localhost:8080/env", HttpMethod.GET, null,
                String.class);
        log.info(result.getBody());
        // should be equal to 6 in reality (X-B3-* headers are repeated three times already)
        assertThat(StringUtils.countMatches(result.getBody(), "spring.cloud.stream.kafka.binder.headers"))
                .isEqualTo(18);
        result = restTemplate.exchange("http://localhost:8080/refresh", HttpMethod.POST, null, String.class);
        // should be zero (refresh returns the number of parameters that changed)
        assertThat(StringUtils.countMatches(result.getBody(), "spring.cloud.stream.kafka.binder.headers")).isEqualTo(6);
        // we have now 24 spring.cloud.stream.kafka.binder.headers instead of 18. Should be 6 overall
        result = restTemplate.exchange("http://localhost:8080/env", HttpMethod.GET, null, String.class);
        assertThat(StringUtils.countMatches(result.getBody(), "spring.cloud.stream.kafka.binder.headers"))
                .isEqualTo(24);
    }

    @SpringCloudApplication
    public static class TestApplication {

        private static Logger log = LoggerFactory.getLogger(TestApplication.class);

        public static void main(String... args) {
            SpringApplication.run(TestApplication.class, args);
        }

    }

}
