package medvedev.com;

import lombok.RequiredArgsConstructor;
import medvedev.com.service.CheckLastExchangeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestEndpoint {

    private final CheckLastExchangeService checkLastExchangeService;

    @GetMapping("/check-price")
    public void checkPrice() {
        checkLastExchangeService.checkLastExchange();
    }
}
