package com.tsuprenko.subscriptionmanager.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class NbpClient {
    private final RestClient restClient;

    public NbpClient() {
        try {

            /// !!!UNSAFE, USED ONLY FOR DEMO!!!

            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() { return null; }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                    }
            };

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new SecureRandom());

            HttpClient httpClient = HttpClient.newBuilder()
                    .sslContext(sslContext)
                    .build();

            this.restClient = RestClient.builder()
                    .baseUrl("https://api.nbp.pl/api/exchangerates/rates/a/")
                    .requestFactory(new JdkClientHttpRequestFactory(httpClient))
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Could not initialize insecure RestClient", e);
        }
    }
    private static final Map<String, BigDecimal> FALLBACK_RATES = Map.of(
            "USD", new BigDecimal("4.00"),
            "EUR", new BigDecimal("4.30"),
            "GBP", new BigDecimal("5.00"),
            "CHF", new BigDecimal("4.50")
    );


    public BigDecimal getExchangeRate(String currencyCode) {
        if ("PLN".equalsIgnoreCase(currencyCode)) {
            return BigDecimal.ONE;
        }

        String code = currencyCode.toUpperCase();
        try {
            NbpResponse response = restClient.get()
                    .uri("/{code}/?format=json", code)
                    .retrieve()
                    .body(NbpResponse.class);

            if (response == null || response.rates().isEmpty()) {
                throw new RuntimeException("Empty response from NBP API");
            }

            return response.rates().get(0).mid();
        } catch (Exception e) {
            BigDecimal fallback = FALLBACK_RATES.getOrDefault(code, new BigDecimal("4.00"));
            log.warn("Failed to fetch exchange rate from NBP for {} due to: {}. Falling back to default rate: {}", 
                     code, e.getMessage(), fallback);
            return fallback;
        }
    }
    private record NbpResponse(List<Rate> rates) {}
    private record Rate(BigDecimal mid) {}
}
