package com.brightline.loyalty;

import com.brightline.loyalty.exceptions.MemberNotFoundException;
import com.brightline.loyalty.points.PointsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** End-to-end checks of the real wiring in the composition root. */
class DemoTest {

    private final PointsService service = Demo.createService();

    @ParameterizedTest(name = "{0}, GHS {1} -> {2} points")
    @CsvSource({
            "m1, 100, 10",
            "m2, 250, 50",
            "m3, 200, 30",
            "m3, 600, 140"
    })
    void wiredServiceAwardsTheSchemePoints(String memberId, String amount, int expectedPoints) {
        assertEquals(expectedPoints, service.calculatePoints(memberId, new BigDecimal(amount)));
    }

    @Test
    void wiredServiceRejectsAnUnknownMember() {
        assertThrows(MemberNotFoundException.class,
                () -> service.calculatePoints("unknown", new BigDecimal("100")));
    }

    @Test
    void mainReportsPointsAndRejectionsInsteadOfFailingSilently() {
        PrintStream original = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));
        try {
            Demo.main(new String[0]);
        } finally {
            System.setOut(original);
        }

        String expected = String.join(System.lineSeparator(),
                "Member m1, GHS 100 -> 10 points",
                "Member m2, GHS 250 -> 50 points",
                "Member m3, GHS 600 -> 140 points",
                "Member unknown, GHS 100 -> rejected: No member found with id unknown",
                "Member m1, GHS -50 -> rejected: Amount must be greater than zero, was -50",
                "");
        assertEquals(expected, captured.toString(StandardCharsets.UTF_8));
    }
}