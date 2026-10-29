package lv.bootcamp.shelter.client;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RestClientPartnerRegistryClientTest {


    private MockWebServer server;
    private RestClientPartnerRegistryClient client;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        client = new RestClientPartnerRegistryClient(RestClient.builder(), server.url("/").toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    void lookup_shouldReturnPartnerRegistryResultWhenSuccessful() throws InterruptedException {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("""
                        {"registered": true, "partnerShelterName": "Test Shelter"}
                        """));

        PartnerRegistryResult result = client.lookup("123456789");
        assertThat(result).isEqualTo(new PartnerRegistryResult(true, "Test Shelter"));
    }

    @Test
    void lookup_shouldThrowWhenPartnerReturnsServiceUnavailable() {
        server.enqueue(new MockResponse().setResponseCode(503));

        assertThatThrownBy(() -> client.lookup("123456789"))
                .isInstanceOf(HttpServerErrorException.ServiceUnavailable.class);
    }


}
