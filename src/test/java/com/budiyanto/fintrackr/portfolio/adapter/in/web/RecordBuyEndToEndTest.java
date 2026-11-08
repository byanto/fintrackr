package com.budiyanto.fintrackr.portfolio.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.budiyanto.fintrackr.TestcontainersConfiguration;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.net.URI;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import({TestcontainersConfiguration.class})
class RecordBuyEndToEndTest {

    private static final HttpHeaders JSON_HTTP_HEADERS = createJsonHttpHeaders();

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Persist buy and keep cash invariant when buy is posted over HTTP")
    void should_persistBuyAndKeepCashInvariant_when_buyIsPostedOverHttp() throws JsonProcessingException {
        // Given
        ResponseEntity<Void> brokerAccountResponse = postBrokerAccount(
                "Test Broker Account", new BigDecimal("0.0020"), new BigDecimal("0.0030"));
        assertThat(brokerAccountResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String brokerAccountId = getId(brokerAccountResponse.getHeaders().getLocation());

        ResponseEntity<Void> portfolioResponse = postPortfolio(brokerAccountId, "Test Portfolio");
        assertThat(portfolioResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String portfolioId = getId(portfolioResponse.getHeaders().getLocation());

        ResponseEntity<Void> depositResponse = postDeposit(portfolioId, new BigDecimal("100000"), "2026-10-15");
        assertThat(depositResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // When
        ResponseEntity<Void> buyResponse = postBuy(
                portfolioId, "ID1000109507", new BigDecimal("100"), "SHARES", new BigDecimal("250"), "2026-10-18");
        assertThat(buyResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String buyId = getId(buyResponse.getHeaders().getLocation());

        // Then
        ResponseEntity<String> buy = getBuy(portfolioId, buyId);
        assertThat(buy.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode buyJson = objectMapper.readTree(buy.getBody());
        assertThat(buyJson.get("id").textValue()).isEqualTo(buyId);
        assertThat(buyJson.get("type").textValue()).isEqualTo("BUY");
        assertThat(buyJson.get("fee").decimalValue()).isEqualByComparingTo("50");

        ResponseEntity<String> portfolio = getPortfolio(portfolioId);
        assertThat(portfolio.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode portfolioJson = objectMapper.readTree(portfolio.getBody());
        assertThat(portfolioJson.get("id").textValue()).isEqualTo(portfolioId);
        assertThat(portfolioJson.get("tradingBalance").decimalValue()).isEqualByComparingTo("74950");

        ResponseEntity<String> brokerAccount = getBrokerAccount(brokerAccountId);
        assertThat(brokerAccount.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode brokerAccountJson = objectMapper.readTree(brokerAccount.getBody());
        assertThat(brokerAccountJson.get("id").textValue()).isEqualTo(brokerAccountId);
        assertThat(brokerAccountJson.get("rdn").decimalValue()).isEqualByComparingTo("74950");
    }

    @Test
    @DisplayName("Return 422 and change nothing when trading balance is insufficient")
    void should_return422AndChangeNothing_when_tradingBalanceIsInsufficient() throws JsonProcessingException {
        // Given
        ResponseEntity<Void> brokerAccountResponse = postBrokerAccount(
                "Test Broker Account", new BigDecimal("0.0020"), new BigDecimal("0.0030"));
        assertThat(brokerAccountResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String brokerAccountId = getId(brokerAccountResponse.getHeaders().getLocation());

        ResponseEntity<Void> portfolioResponse = postPortfolio(brokerAccountId, "Test Portfolio");
        assertThat(portfolioResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String portfolioId = getId(portfolioResponse.getHeaders().getLocation());

        ResponseEntity<Void> depositResponse = postDeposit(portfolioId, new BigDecimal("20000"), "2026-10-15");
        assertThat(depositResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // When & Then
        ResponseEntity<Void> buyResponse = postBuy(
                portfolioId, "ID1000109507", new BigDecimal("100"), "SHARES", new BigDecimal("250"), "2026-10-18");
        assertThat(buyResponse.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

        ResponseEntity<String> portfolio = getPortfolio(portfolioId);
        assertThat(portfolio.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode portfolioJson = objectMapper.readTree(portfolio.getBody());
        assertThat(portfolioJson.get("id").textValue()).isEqualTo(portfolioId);
        assertThat(portfolioJson.get("tradingBalance").decimalValue()).isEqualByComparingTo("20000");

        ResponseEntity<String> brokerAccount = getBrokerAccount(brokerAccountId);
        assertThat(brokerAccount.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode brokerAccountJson = objectMapper.readTree(brokerAccount.getBody());
        assertThat(brokerAccountJson.get("id").textValue()).isEqualTo(brokerAccountId);
        assertThat(brokerAccountJson.get("rdn").decimalValue()).isEqualByComparingTo("20000");

        ResponseEntity<String> transactions = listTransactions(portfolioId);
        assertThat(transactions.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode transactionsJson = objectMapper.readTree(transactions.getBody());
        assertThat(transactionsJson.get("items").size()).isEqualTo(1);
        assertThat(transactionsJson.get("items").get(0).get("type").textValue()).isEqualTo("DEPOSIT");
    }

    private static HttpHeaders createJsonHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private String getId(URI uri) {
        String path = uri.getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }

    private ResponseEntity<Void> postBrokerAccount(String name, BigDecimal buyRate, BigDecimal sellRate) {
        Map<String, Object> bodyMap = Map.of(
                "name", name,
                "buyRate", buyRate,
                "sellRate", sellRate
        );

        return restTemplate.postForEntity("/api/broker-accounts", new HttpEntity<>(bodyMap, JSON_HTTP_HEADERS), Void.class);
    }

    private ResponseEntity<Void> postPortfolio(String brokerAccountId, String name) {
        Map<String, Object> bodyMap = Map.of(
                "brokerAccountId", brokerAccountId,
                "name", name
        );

        return restTemplate.postForEntity("/api/portfolios", new HttpEntity<>(bodyMap, JSON_HTTP_HEADERS), Void.class);
    }

    private ResponseEntity<Void> postDeposit(String portfolioId, BigDecimal amount, String date) {
        Map<String, Object> bodyMap = Map.of(
                "amount", amount,
                "date", date
        );

        return restTemplate.postForEntity("/api/portfolios/{pid}/deposits", new HttpEntity<>(bodyMap, JSON_HTTP_HEADERS), Void.class, portfolioId);
    }

    private ResponseEntity<Void> postBuy(String portfolioId, String isin, BigDecimal quantity, String quantityKind, BigDecimal price, String date) {
        Map<String, Object> bodyMap = Map.of(
                "isin", isin,
                "quantity", quantity,
                "quantityKind", quantityKind,
                "price", price,
                "date", date
        );

        return restTemplate.postForEntity("/api/portfolios/{pid}/buys", new HttpEntity<>(bodyMap, JSON_HTTP_HEADERS), Void.class, portfolioId);
    }

    private ResponseEntity<String> getBuy(String portfolioId, String transactionId) {
        return restTemplate.getForEntity(
                "/api/portfolios/{pid}/transactions/{tid}",
                String.class,
                portfolioId, transactionId);
    }

    private ResponseEntity<String> getPortfolio(String portfolioId) {
        return restTemplate.getForEntity(
                "/api/portfolios/{pid}",
                String.class,
                portfolioId);
    }

    private ResponseEntity<String> getBrokerAccount(String brokerAccountId) {
        return restTemplate.getForEntity(
                "/api/broker-accounts/{id}",
                String.class,
                brokerAccountId);
    }

    private ResponseEntity<String> listTransactions(String portfolioId) {
        return restTemplate.getForEntity(
                "/api/portfolios/{pid}/transactions",
                String.class,
                portfolioId);
    }

}
