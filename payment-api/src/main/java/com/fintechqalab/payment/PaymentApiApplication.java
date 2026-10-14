package com.fintechqalab.payment;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Payment API used as the system under test. Uses only the JDK (no frameworks).
 *
 * <pre>
 * GET  /health                -> 200 {"status":"UP"}
 * POST /api/auth/token        -> 200 {"token":"..."}       body: {"username":"demo","password":"demo123"}
 * POST /api/payments          -> 201 payment (auth)        body: {"amount":100.00,"currency":"EUR","iban":"IE29AIBK93115212345678","instant":false}
 * GET  /api/payments          -> 200 list of payments      (auth required)
 * GET  /api/payments/{id}     -> 200 payment or 404        (auth required)
 * </pre>
 *
 * Errors are returned as {"error":"..."} with 400 (invalid input), 401 (missing or bad token),
 * 404 (not found) or 405 (method not allowed).
 *
 * Start it from IntelliJ (run main) and it listens on http://localhost:8080 (override with PORT).
 */
public class PaymentApiApplication {

    private static final String DEMO_USER = "demo";
    private static final String DEMO_PASSWORD = "demo123";

    private final FeeCalculator feeCalculator = new FeeCalculator();
    private final IbanValidator ibanValidator = new IbanValidator();
    private final Map<Long, Payment> payments = new ConcurrentHashMap<>();
    private final Set<String> tokens = ConcurrentHashMap.newKeySet();
    private final AtomicLong nextId = new AtomicLong(1);

    private HttpServer server;

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        new PaymentApiApplication().start(port);
        System.out.println("Payment API running on http://localhost:" + port);
    }

    public void start(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/health", exchange -> handle(exchange, this::health));
        server.createContext("/api/auth/token", exchange -> handle(exchange, this::token));
        server.createContext("/api/payments", exchange -> handle(exchange, this::payments));
        server.start();
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    // ---- handlers ----------------------------------------------------------------------------

    private Response health(HttpExchange exchange) {
        return Response.ok(Map.of("status", "UP"));
    }

    private Response token(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            return Response.error(405, "Method not allowed");
        }
        Map<String, Object> body = Json.parseObject(readBody(exchange));
        if (DEMO_USER.equals(body.get("username")) && DEMO_PASSWORD.equals(body.get("password"))) {
            String token = UUID.randomUUID().toString();
            tokens.add(token);
            return Response.ok(Map.of("token", token));
        }
        return Response.error(401, "Invalid username or password");
    }

    private Response payments(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String rest = path.substring("/api/payments".length());

        if (rest.isEmpty() || rest.equals("/")) {
            if ("POST".equals(method)) {
                if (!isAuthorized(exchange)) {
                    return Response.error(401, "Missing or invalid token");
                }
                return createPayment(exchange);
            }
            if ("GET".equals(method)) {
                List<Map<String, Object>> list = new ArrayList<>();
                payments.values().stream()
                        .sorted((a, b) -> Long.compare(a.id(), b.id()))
                        .forEach(p -> list.add(p.toMap()));
                return new Response(200, list);
            }
            return Response.error(405, "Method not allowed");
        }

        if (!"GET".equals(method)) {
            return Response.error(405, "Method not allowed");
        }
        if (!isAuthorized(exchange)) {
            return Response.error(401, "Missing or invalid token");
        }
        long id = parseId(rest.substring(1));
        Payment payment = payments.get(id);
        if (payment == null) {
            return Response.error(404, "Payment " + id + " not found");
        }
        return Response.ok(payment.toMap());
    }

    private Response createPayment(HttpExchange exchange) throws IOException {
        Map<String, Object> body = Json.parseObject(readBody(exchange));

        if (!(body.get("amount") instanceof BigDecimal amount)) {
            return Response.error(400, "amount is required and must be a number");
        }
        if (!(body.get("currency") instanceof String currency)) {
            return Response.error(400, "currency is required");
        }
        if (!(body.get("iban") instanceof String iban) || !ibanValidator.isValid(iban)) {
            return Response.error(400, "iban is missing or invalid");
        }
        boolean instant = Boolean.TRUE.equals(body.get("instant"));

        BigDecimal fee = feeCalculator.calculateFee(amount, currency, instant);
        Payment payment = new Payment(nextId.getAndIncrement(), amount, currency,
                iban.replace(" ", ""), instant, fee, "CREATED", Instant.now());
        payments.put(payment.id(), payment);
        return new Response(201, payment.toMap());
    }

    // ---- plumbing ----------------------------------------------------------------------------

    private static long parseId(String raw) {
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Could not parse id " + raw, e);
        }
    }

    private boolean isAuthorized(HttpExchange exchange) {
        String header = exchange.getRequestHeaders().getFirst("Authorization");
        return header != null && header.startsWith("Bearer ") && tokens.contains(header.substring(7));
    }

    private static String readBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    private void handle(HttpExchange exchange, Handler handler) throws IOException {
        Response response;
        try {
            response = handler.handle(exchange);
        } catch (IllegalArgumentException e) {
            response = Response.error(400, e.getMessage());
        } catch (Exception e) {
            response = Response.error(500, "Internal server error");
        }
        byte[] bytes = Json.write(response.body()).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(response.status(), bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    @FunctionalInterface
    private interface Handler {
        Response handle(HttpExchange exchange) throws IOException;
    }

    private record Response(int status, Object body) {
        static Response ok(Object body) {
            return new Response(200, body);
        }

        static Response error(int status, String message) {
            return new Response(status, Map.of("error", message == null ? "Error" : message));
        }
    }
}
