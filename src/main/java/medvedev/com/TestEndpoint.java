package medvedev.com;

import lombok.RequiredArgsConstructor;
import medvedev.com.service.CheckLastExchangeService;
import medvedev.com.service.NeuralNetworkService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestEndpoint {

    private final CheckLastExchangeService checkLastExchangeService;
    private final NeuralNetworkService neuralNetworkService;

    @GetMapping("/check-price")
    public void checkPrice() {
        checkLastExchangeService.checkLastExchange();
    }

    @GetMapping("/nn")
    public void nn() {
        neuralNetworkService.run();
    }
}
