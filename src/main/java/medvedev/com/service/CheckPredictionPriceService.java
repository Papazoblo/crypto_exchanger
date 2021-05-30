package medvedev.com.service;

import lombok.RequiredArgsConstructor;
import medvedev.com.dto.PriceChangeDto;
import medvedev.com.exception.EntityNotFoundException;
import medvedev.com.service.exchangefactory.ExchangeStrategy;
import medvedev.com.service.exchangefactory.FiatCryptExchangeStrategy;
import medvedev.com.wrapper.BigDecimalWrapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CheckPredictionPriceService {

    private final NeuralNetworkService neuralNetworkService;
    private final PricePredictionHistoryService pricePredictionHistoryService;

    public void checkPrediction(PriceChangeDto priceChangeDto, ExchangeStrategy strategy) {

        BigDecimalWrapper prediction = new BigDecimalWrapper(neuralNetworkService.run()[0]);
        BigDecimalWrapper lastPrediction;
        try {
            lastPrediction = pricePredictionHistoryService.getLast();
            if (strategy instanceof FiatCryptExchangeStrategy) {
                if (prediction.isLessThen(lastPrediction)) {
                    pricePredictionHistoryService.save(prediction);
                } else {
                    launchExchange(priceChangeDto, strategy);
                }
            } else {
                if (prediction.isGreaterThen(lastPrediction)) {
                    pricePredictionHistoryService.save(prediction);
                } else {
                    launchExchange(priceChangeDto, strategy);
                }
            }
        } catch (EntityNotFoundException ex) {
            pricePredictionHistoryService.save(prediction);
        }
    }

    private void launchExchange(PriceChangeDto priceChange, ExchangeStrategy strategy) {
        strategy.launchExchangeAlgorithm(priceChange);
        pricePredictionHistoryService.deleteAll();
    }
}
