package org.delcom.ifs25045springbootcashflow.adapter.ai;

import com.sun.net.httpserver.HttpServer;
import org.delcom.ifs25045springbootcashflow.domain.entity.AiGeneratedTransaction;
import org.delcom.ifs25045springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25045springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25045springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OpenAiFinanceServiceTest {

    private HttpServer server;
    private String serverUrl;
    private String mockResponseBody;
    private int mockResponseCode;
    private String lastRequestBody;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/chat/completions", exchange -> {
            lastRequestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            byte[] bytes = mockResponseBody != null ? mockResponseBody.getBytes(StandardCharsets.UTF_8) : new byte[0];
            exchange.sendResponseHeaders(mockResponseCode, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });
        server.start();
        serverUrl = "http://localhost:" + server.getAddress().getPort();
        mockResponseCode = 200;
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    /** Membungkus isi konten AI ke dalam respon chat completions. */
    private String wrap(String content) {
        ObjectMapper mapper = new ObjectMapper();
        String escaped = mapper.writeValueAsString(content);
        return "{\"choices\":[{\"message\":{\"content\":" + escaped + "}}]}";
    }

    @Test
    void generatesTransactionsSuccessfully() throws Exception {
        mockResponseBody = wrap("[{\"description\":\"Gaji\",\"amount\":5000000,\"type\":\"INCOME\"},"
                + "{\"description\":\"Makan\",\"amount\":\"25000\",\"type\":\"expense\"}]");

        OpenAiFinanceService service = new OpenAiFinanceService(serverUrl, "valid-key", "test-model");
        List<AiGeneratedTransaction> result = service.generateTransactions("gajian lalu makan");

        assertEquals(2, result.size());
        assertEquals("Gaji", result.get(0).description());
        assertEquals(5_000_000, result.get(0).amount());
        assertEquals(TransactionType.INCOME, result.get(0).type());
        assertEquals(25_000, result.get(1).amount());
        assertEquals(TransactionType.EXPENSE, result.get(1).type());
        assertNotNull(lastRequestBody);
        assertEquals(true, lastRequestBody.contains("test-model"));
    }

    @Test
    void skipsInvalidItemsAndHandlesDecimalAmounts() throws Exception {
        mockResponseBody = wrap("```json\n["
                + "{\"description\":\"Valid\",\"amount\":15000.0,\"type\":\"Pemasukan\"},"
                + "{\"description\":\"Tanpa tipe\",\"amount\":10},"
                + "{},"
                + "{\"description\":\"Tanpa jumlah\",\"type\":\"INCOME\"},"
                + "{\"description\":\"Jumlah teks\",\"amount\":\"abc\",\"type\":\"INCOME\"},"
                + "{\"description\":\"Jumlah nol\",\"amount\":0,\"type\":\"INCOME\"},"
                + "{\"description\":\"Tipe asing\",\"amount\":10,\"type\":\"transfer\"},"
                + "{\"description\":\"  \",\"amount\":10,\"type\":\"INCOME\"}"
                + "]\n```");

        OpenAiFinanceService service = new OpenAiFinanceService(serverUrl + "/", "valid-key", "   ");
        List<AiGeneratedTransaction> result = service.generateTransactions("catatan");
        assertEquals(1, result.size());
        assertEquals(15_000, result.get(0).amount());
        assertEquals(TransactionType.INCOME, result.get(0).type());
    }

    @Test
    void handlesCodeFenceVariants() throws Exception {
        OpenAiFinanceService service = new OpenAiFinanceService(serverUrl, "valid-key", "model");
        String item = "[{\"description\":\"A\",\"amount\":1,\"type\":\"INCOME\"}]";

        mockResponseBody = wrap("```" + item);
        assertEquals(1, service.generateTransactions("x").size());

        mockResponseBody = wrap("```" + item + "```");
        assertEquals(1, service.generateTransactions("x").size());

        mockResponseBody = wrap("```\n" + item + "\n```");
        assertEquals(1, service.generateTransactions("x").size());

        mockResponseBody = wrap("```json\n" + item);
        assertEquals(1, service.generateTransactions("x").size());
    }

    @Test
    void handlesEndpointAlreadyEndingWithChatCompletionsAndNullModel() throws Exception {
        mockResponseBody = wrap("[{\"description\":\"A\",\"amount\":1,\"type\":\"INCOME\"}]");
        OpenAiFinanceService service = new OpenAiFinanceService(serverUrl + "/chat/completions", "valid-key", null);
        assertEquals(1, service.generateTransactions("x").size());
        assertEquals(true, lastRequestBody.contains("gpt-4o-mini"));
    }

    @Test
    void rejectsInvalidInputs() {
        OpenAiFinanceService service = new OpenAiFinanceService("http://localhost", "valid-key", "model");
        assertThrows(ValidationException.class, () -> service.generateTransactions(null));
        assertThrows(ValidationException.class, () -> service.generateTransactions("   "));

        assertThrows(ValidationException.class,
                () -> new OpenAiFinanceService(null, "valid-key", "model").generateTransactions("x"));
        assertThrows(ValidationException.class,
                () -> new OpenAiFinanceService("   ", "valid-key", "model").generateTransactions("x"));
        assertThrows(ValidationException.class,
                () -> new OpenAiFinanceService("http://localhost", null, "model").generateTransactions("x"));
        assertThrows(ValidationException.class,
                () -> new OpenAiFinanceService("http://localhost", "   ", "model").generateTransactions("x"));
    }

    @Test
    void throwsOnHttpError() {
        mockResponseCode = 500;
        mockResponseBody = "Internal Server Error";

        OpenAiFinanceService service = new OpenAiFinanceService(serverUrl, "valid-key", "test-model");
        DomainException ex = assertThrows(DomainException.class, () -> service.generateTransactions("x"));
        assertEquals(true, ex.getMessage().contains("500"));
    }

    @Test
    void throwsOnMissingChoicesOrContent() {
        OpenAiFinanceService service = new OpenAiFinanceService(serverUrl, "valid-key", "test-model");

        mockResponseBody = "{}";
        assertThrows(DomainException.class, () -> service.generateTransactions("x"));
        mockResponseBody = "{\"choices\": []}";
        assertThrows(DomainException.class, () -> service.generateTransactions("x"));
        mockResponseBody = "{\"choices\": [{}]}";
        assertThrows(DomainException.class, () -> service.generateTransactions("x"));
        mockResponseBody = "{\"choices\": [{\"message\": {}}]}";
        assertThrows(DomainException.class, () -> service.generateTransactions("x"));
    }

    @Test
    void throwsWhenContentIsNotArrayEmptyOrInvalidJson() {
        OpenAiFinanceService service = new OpenAiFinanceService(serverUrl, "valid-key", "test-model");

        mockResponseBody = wrap("{\"single\": 1}");
        assertThrows(DomainException.class, () -> service.generateTransactions("x"));
        mockResponseBody = wrap("[]");
        assertThrows(DomainException.class, () -> service.generateTransactions("x"));
        mockResponseBody = wrap("[{\"description\":\"\",\"amount\":1,\"type\":\"INCOME\"}]");
        assertThrows(DomainException.class, () -> service.generateTransactions("x"));
        mockResponseBody = wrap("invalid json syntax");
        assertThrows(DomainException.class, () -> service.generateTransactions("x"));
    }

    @Test
    void handlesHttpClientExceptions() throws Exception {
        HttpClient mockClient = mock(HttpClient.class);
        when(mockClient.send(any(HttpRequest.class), any())).thenThrow(new IOException("Connection refused"));

        OpenAiFinanceService service =
                new OpenAiFinanceService("http://localhost:1234", "key", "model", mockClient, new ObjectMapper());
        assertThrows(DomainException.class, () -> service.generateTransactions("Prompt"));

        when(mockClient.send(any(HttpRequest.class), any())).thenThrow(new InterruptedException("Interrupted"));
        assertThrows(DomainException.class, () -> service.generateTransactions("Prompt"));
        Thread.interrupted(); // bersihkan flag interrupt agar tidak mengganggu tes lain
    }

    @Test
    void handlesSerializationError() {
        ObjectMapper mockMapper = mock(ObjectMapper.class);
        when(mockMapper.writeValueAsString(any())).thenThrow(new RuntimeException("Serialization failure"));

        OpenAiFinanceService service =
                new OpenAiFinanceService("http://localhost:1234", "key", "model", HttpClient.newHttpClient(), mockMapper);
        assertThrows(DomainException.class, () -> service.generateTransactions("Prompt"));
    }
}
