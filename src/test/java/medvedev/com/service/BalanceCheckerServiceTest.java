package medvedev.com.service;

import medvedev.com.enums.SystemConfiguration;
import medvedev.com.exception.MinMaxAmountIsNotValidException;
import medvedev.com.exception.NotEnoughFundsBalanceException;
import medvedev.com.wrapper.BigDecimalWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BalanceCheckerServiceTest {

    private SystemConfigurationService systemConfigurationService;
    private BalanceCheckerService balanceCheckerService;

    @BeforeEach
    void setUp() {
        systemConfigurationService = mock(SystemConfigurationService.class);
        balanceCheckerService = new BalanceCheckerService(systemConfigurationService);
    }

    @Nested
    class IsEnoughFundsBalance {

        private BigDecimalWrapper minAmount;
        private BigDecimalWrapper maxAmount;
        private BigDecimalWrapper balance;

        @BeforeEach
        void setUp() {
            minAmount = new BigDecimalWrapper(10);
            maxAmount = new BigDecimalWrapper(50);

            when(systemConfigurationService.findBdByName(SystemConfiguration.MIN_AMOUNT_EXCHANGE))
                    .thenReturn(minAmount);
            when(systemConfigurationService.findBdByName(SystemConfiguration.MAX_AMOUNT_EXCHANGE))
                    .thenReturn(maxAmount);
        }

        @Nested
        class ThrowException {

            private String amount;

            @Nested
            class ThrowNotEnoughFundsBalanceException {

                @BeforeEach
                void setUp() {
                    amount = "5";
                }

                @Test
                void shouldThrowException() {
                    balance = new BigDecimalWrapper(amount);

                    assertThrows(NotEnoughFundsBalanceException.class, () ->
                            balanceCheckerService.isEnoughFundsBalance(balance));
                }

                @Test
                void shouldThrowExceptionWhenAmountInString() {
                    assertThrows(NotEnoughFundsBalanceException.class, () ->
                            balanceCheckerService.isEnoughFundsBalance(amount));
                }
            }

            @Nested
            class ThrowMinMaxAmountIsNotValidException {

                @BeforeEach
                void setUp() {
                    amount = "5";
                }

                @Nested
                class WhenAmountIsEquals {

                    @BeforeEach
                    void setUp() {
                        minAmount = new BigDecimalWrapper(10);
                        maxAmount = new BigDecimalWrapper(10);

                        when(systemConfigurationService.findBdByName(SystemConfiguration.MIN_AMOUNT_EXCHANGE))
                                .thenReturn(minAmount);
                        when(systemConfigurationService.findBdByName(SystemConfiguration.MAX_AMOUNT_EXCHANGE))
                                .thenReturn(maxAmount);
                    }

                    @Test
                    void shouldThrowException() {
                        balance = new BigDecimalWrapper(amount);

                        assertThrows(MinMaxAmountIsNotValidException.class, () ->
                                balanceCheckerService.isEnoughFundsBalance(balance));
                    }

                    @Test
                    void shouldThrowExceptionWhenAmountInString() {
                        assertThrows(MinMaxAmountIsNotValidException.class, () ->
                                balanceCheckerService.isEnoughFundsBalance(amount));
                    }
                }

                @Nested
                class WhenAmountIsNotEquals {


                    @BeforeEach
                    void setUp() {
                        minAmount = new BigDecimalWrapper(15);
                        maxAmount = new BigDecimalWrapper(10);

                        when(systemConfigurationService.findBdByName(SystemConfiguration.MIN_AMOUNT_EXCHANGE))
                                .thenReturn(minAmount);
                        when(systemConfigurationService.findBdByName(SystemConfiguration.MAX_AMOUNT_EXCHANGE))
                                .thenReturn(maxAmount);
                    }

                    @Test
                    void shouldThrowException() {
                        balance = new BigDecimalWrapper(amount);

                        assertThrows(MinMaxAmountIsNotValidException.class, () ->
                                balanceCheckerService.isEnoughFundsBalance(balance));
                    }

                    @Test
                    void shouldThrowExceptionWhenAmountInString() {
                        assertThrows(MinMaxAmountIsNotValidException.class, () ->
                                balanceCheckerService.isEnoughFundsBalance(amount));
                    }
                }
            }
        }

        @Nested
        class ReturnAmountWhenBalanceIsLessThenMaxAmount {

            private String amount;

            @BeforeEach
            void setUp() {
                amount = "25";
            }

            @Test
            void shouldReturnAmountWhenAmountInString() {
                balance = new BigDecimalWrapper(amount);

                BigDecimalWrapper actual = balanceCheckerService.isEnoughFundsBalance(amount);
                assertEquals(balance, actual);
            }

            @Test
            void shouldReturnWhenAmount() {
                balance = new BigDecimalWrapper(amount);

                BigDecimalWrapper actual = balanceCheckerService.isEnoughFundsBalance(balance);
                assertEquals(balance, actual);
            }
        }

        @Nested
        class ReturnAmountToExchangeWhenBalanceIsGreaterThenMaxAmount {

            private String amount;

            @BeforeEach
            void setUp() {
                amount = "55";
            }

            @Test
            void shouldReturnAmount() {
                balance = new BigDecimalWrapper(55);

                BigDecimalWrapper actual = balanceCheckerService.isEnoughFundsBalance(balance);
                assertEquals(maxAmount, actual);
            }

            @Test
            void shouldReturnAmountWhenAmountInString() {
                balance = new BigDecimalWrapper(amount);

                BigDecimalWrapper actual = balanceCheckerService.isEnoughFundsBalance(amount);
                assertEquals(maxAmount, actual);
            }
        }
    }
}
