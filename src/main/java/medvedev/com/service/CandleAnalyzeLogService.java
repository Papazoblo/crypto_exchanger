package medvedev.com.service;

import lombok.RequiredArgsConstructor;
import medvedev.com.entity.CandleAnalyzeLogEntity;
import medvedev.com.entity.PriceHistoryBlockEntity;
import medvedev.com.enums.OrderSide;
import medvedev.com.repository.CandleAnalyzeLogRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CandleAnalyzeLogService {

    private final CandleAnalyzeLogRepository repository;

    public void save(PriceHistoryBlockEntity first,
                     PriceHistoryBlockEntity middle,
                     PriceHistoryBlockEntity last,
                     String price, OrderSide type) {
        CandleAnalyzeLogEntity entity = new CandleAnalyzeLogEntity();
        entity.setPrice(price);
        entity.setType(type);
        entity.setFirstBlockId(first.getId());
        entity.setMiddleBlockId(middle.getId());
        entity.setLastBlockId(last.getId());
        repository.save(entity);
    }

    public Optional<CandleAnalyzeLogEntity> getLast() {
        return repository.findFirstByIdIsNotNullOrderByIdDesc();
    }
}
