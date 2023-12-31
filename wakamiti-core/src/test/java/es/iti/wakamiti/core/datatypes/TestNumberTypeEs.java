/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package es.iti.wakamiti.core.datatypes;

import es.iti.wakamiti.api.WakamitiException;
import static es.iti.wakamiti.core.BasicDataTypes.*;
import java.math.BigDecimal;
import java.util.Locale;
import org.junit.jupiter.api.*;


class TestNumberTypeEs {

    private static final Locale LOCALE = Locale.forLanguageTag("es");


    @Test
    void testAttemptParseWithWrongValue() {
        Assertions.assertThrowsExactly(
            WakamitiException.class,
            () -> NUMBER.parse(LOCALE, "xxxx"),
            "Error parsing type int using language en: 'xxxxx'"
        );
    }


    @Test
    void testNumberDataType() {
        var type = NUMBER;
        Assertions.assertTrue(type.matcher(LOCALE, "12345").matches());
        Assertions.assertEquals(12345, type.parse(LOCALE, "12345"));
        Assertions.assertTrue(type.matcher(LOCALE, "12.345").matches());
        Assertions.assertEquals(12345, type.parse(LOCALE, "12.345"));
        Assertions.assertFalse(type.matcher(LOCALE, "12.345,54").matches());
        Assertions.assertFalse(type.matcher(LOCALE, "xxxxx").matches());
    }


    @Test
    void testDecimalDataType() {
        var type = DECIMAL;
        Assertions.assertFalse(type.matcher(LOCALE, "12345").matches());
        Assertions.assertTrue(type.matcher(LOCALE, "12345,0").matches());
        Assertions.assertEquals(BigDecimal.valueOf(12345.0),type.parse(LOCALE, "12345,0"));
        Assertions.assertFalse(type.matcher(LOCALE, "12.345").matches());
        Assertions.assertTrue(type.matcher(LOCALE, "12.345,0").matches());
        Assertions.assertEquals(BigDecimal.valueOf(12345.0), type.parse(LOCALE, "12.345,0"));
        Assertions.assertTrue(type.matcher(LOCALE, "12.345,54").matches());
        Assertions.assertEquals(BigDecimal.valueOf(12345.54),type.parse(LOCALE, "12.345,54"));
        Assertions.assertFalse(type.matcher(LOCALE, "xxxxx").matches());
    }

}
