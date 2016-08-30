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
        assertThat(StringUtils.countMatches(result.getBody(), "spring.cloud.stream.kafka.binder.headers")).isEqualTo(6);
        result = restTemplate.exchange("http://localhost:8080/refresh", HttpMethod.POST, null, String.class);
        assertThat(StringUtils.countMatches(result.getBody(), "spring.cloud.stream.kafka.binder.headers")).isEqualTo(0);
        result = restTemplate.exchange("http://localhost:8080/env", HttpMethod.GET, null, String.class);
        assertThat(StringUtils.countMatches(result.getBody(), "spring.cloud.stream.kafka.binder.headers")).isEqualTo(6);
    }

    @SpringCloudApplication
    public static class TestApplication {

        public static void main(String... args) {
            SpringApplication.run(TestApplication.class, args);
        }

    }

}
