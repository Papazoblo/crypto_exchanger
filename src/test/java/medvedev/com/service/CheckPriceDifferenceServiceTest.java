package medvedev.com.service;

import medvedev.com.enums.SystemConfiguration;
import medvedev.com.wrapper.BigDecimalWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CheckPriceDifferenceServiceTest {

    private double recordPrice;
    private double differencePrice;

    private SystemConfigurationService configurationService;
    private CheckPriceDifferenceService service;

    @BeforeEach
    void setUp() {
        configurationService = mock(SystemConfigurationService.class);
        service = new CheckPriceDifferenceService(configurationService);

        recordPrice = 2000;
        differencePrice = 10;

        when(configurationService.findDoubleByName(any(SystemConfiguration.class)))
                .thenReturn(differencePrice);
    }

    @Nested
    class ReturnTrue {

        @Test
        void shouldCheckIncreased() {
            BigDecimalWrapper lastPrice = new BigDecimalWrapper(2500);

            boolean actual = service.isPriceIncreased(lastPrice, recordPrice);
            assertTrue(actual);
        }

        @Test
        void shouldCheckDecreased() {
            BigDecimalWrapper lastPrice = new BigDecimalWrapper(1500);

            boolean actual = service.isPriceDecreased(lastPrice, recordPrice);
            assertTrue(actual);
        }
    }

    @Nested
    class ReturnFalse {

        @Test
        void shouldCheckIncreased() {
            BigDecimalWrapper lastPrice = new BigDecimalWrapper(1500);

            boolean actual = service.isPriceIncreased(lastPrice, recordPrice);
            assertFalse(actual);
        }

        @Test
        void shouldCheckDecreased() {
            BigDecimalWrapper lastPrice = new BigDecimalWrapper(2500);

            boolean actual = service.isPriceDecreased(lastPrice, recordPrice);
            assertFalse(actual);
        }
    }
}
