package medvedev.com;

import medvedev.com.client.BinanceApiClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableFeignClients(clients = {BinanceApiClient.class})
public class Application {

    public static void main(String... args) {

        SpringApplication.run(Application.class);

        /*
        1. Фиксируем курс

        2. Создаем ордер на покупку (раз в 2 минуты)
        - Проверяем (если нет открытого ордера и последний исполненный это ордер на ПРОДАЖУ)
        - ищем курс по которому больше всего заявок
        - создаем заявку

        3.
         */
    }
}
