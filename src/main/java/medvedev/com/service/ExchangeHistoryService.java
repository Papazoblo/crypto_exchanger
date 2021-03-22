package medvedev.com.service;

import com.binance.api.client.domain.OrderSide;
import com.binance.api.client.domain.OrderStatus;
import lombok.RequiredArgsConstructor;
import medvedev.com.dto.ExchangeHistoryDto;
import medvedev.com.entity.ExchangeHistoryEntity;
import medvedev.com.enums.SystemConfiguration;
import medvedev.com.exception.EntityNotFoundException;
import medvedev.com.repository.ExchangeHistoryRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExchangeHistoryService {

    private final ExchangeHistoryRepository exchangeHistoryRepository;
    private final SystemConfigurationService systemConfigurationService;
    private final TimeService timeService;

    public List<ExchangeHistoryDto> getAllOpenExchange() {
        return toDto(exchangeHistoryRepository.findAllByOperationTypeAndOrderStatusAndIdPrevExchangeIsNull(OrderSide.BUY,
                OrderStatus.FILLED));
    }

    public List<ExchangeHistoryDto> getOpenProfitableExchange(BigDecimal lastPrice) {
        return getAllOpenExchange().stream()
                .filter(record -> record.getPrice().isLessThen(lastPrice))
                .collect(Collectors.toList());
    }

    public List<ExchangeHistoryDto> getAllExchange() {
        return toDto(exchangeHistoryRepository.findAll());
    }

    public ExchangeHistoryDto getExchangeById(Long id) {
        return ExchangeHistoryDto.from(exchangeHistoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ExchangeHistoryEntity", id)));
    }

    public List<ExchangeHistoryDto> findLastExchangeFiatCryptInTimeRange() {
        Integer minutes = systemConfigurationService.findIntegerByName(SystemConfiguration.MIN_MINUTES_SPACE_BETWEEN_EXCHANGE);
        LocalDateTime startTime = timeService.withoutMinusMinutes(minutes);
        return toDto(exchangeHistoryRepository.findAllByDateTimeGreaterThanAndIdPrevExchangeIsNotNullAndOrderStatus(
                startTime, OrderStatus.FILLED));
    }

    private static List<ExchangeHistoryDto> toDto(List<ExchangeHistoryEntity> entities) {
        return entities.stream()
                .map(ExchangeHistoryDto::from)
                .collect(Collectors.toList());
    }
}
