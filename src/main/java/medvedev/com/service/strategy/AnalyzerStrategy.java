package medvedev.com.service.strategy;

import medvedev.com.entity.PriceHistoryBlockEntity;

import java.util.List;

public interface AnalyzerStrategy {

    boolean isFound(List<PriceHistoryBlockEntity> blockList);
}
