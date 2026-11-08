package com.fintechqalab.qa.ui;

import com.fintechqalab.payment.PaymentApiApplication;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.net.ServerSocket;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Shared setup for every Playwright UI test.
 *
 * Before the tests of a class: starts the payment API on a free port and opens Chromium.
 * Before each test: opens a fresh browser context (clean cookies) and goes to the payment page.
 * After: closes everything again.
 *
 * Run with a visible browser: mvn test -pl qa-tests -am -Dheadless=false
 */
public abstract class BaseUiTest {

    protected static PaymentApiApplication app;
    protected static String baseUrl;

    private static Playwright playwright;
    private static Browser browser;

    protected BrowserContext context;
    protected Page page;

    @BeforeAll
    static void startAppAndBrowser() throws IOException {
        int port = freePort();
        app = new PaymentApiApplication();
        app.start(port);
        baseUrl = "http://localhost:" + port;

        playwright = Playwright.create();
        boolean headless = !"false".equals(System.getProperty("headless"));
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(headless));
    }

    @AfterAll
    static void stopAppAndBrowser() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
        if (app != null) {
            app.stop();
        }
    }

    @BeforeEach
    void openPaymentPage() {
        context = browser.newContext();
        page = context.newPage();
        page.navigate(baseUrl + "/");
    }

    @AfterEach
    void closePage() {
        if (context != null) {
            context.close();
        }
    }

    /** Signs in with the demo user and waits until the payment form is visible. */
    protected void loginAsDemoUser() {
        page.getByTestId("username").fill("demo");
        page.getByTestId("password").fill("demo123");
        page.getByTestId("login-button").click();
        assertThat(page.getByTestId("payment-form")).isVisible();
    }

    private static int freePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }
}
