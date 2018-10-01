package voidchess.player.ki.evaluation;

import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;


public class EvaluatedTest {
    @Test
    public void testCloseTo() {
        Evaluated value1 = getValue(1f, 5f);
        Evaluated value2 = getValue(2.1f, 3.9f);
        Evaluated value3 = getValue(1.5f, 0f);

        assertFalse(value1.isCloseToByPrimary(value2));
        assertTrue(value1.isCloseToByPrimary(value3));
        assertTrue(value1.isCloseToByCombined(value2));
        assertFalse(value1.isCloseToByCombined(value3));

        assertFalse(value2.isCloseToByPrimary(value1));
        assertTrue(value2.isCloseToByPrimary(value3));
        assertTrue(value2.isCloseToByCombined(value1));
        assertFalse(value2.isCloseToByCombined(value3));

        assertTrue(value3.isCloseToByPrimary(value2));
        assertTrue(value3.isCloseToByPrimary(value1));
        assertFalse(value3.isCloseToByCombined(value2));
        assertFalse(value3.isCloseToByCombined(value1));
    }

    private Ongoing getValue(double primary, double secondary) {
        Ongoing value = new Ongoing(primary);
        value.setSecondaryEvaluation(secondary);
        return value;
    }
}
