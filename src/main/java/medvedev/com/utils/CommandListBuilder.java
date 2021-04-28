package medvedev.com.utils;

import lombok.experimental.UtilityClass;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;

import java.util.Arrays;
import java.util.List;

@UtilityClass
public class CommandListBuilder {

    public static List<BotCommand> getCommandList() {
        return Arrays.asList(
                createCommand("/start", "Authenticated command"),
                createCommand("/launched", "Launch service"),
                createCommand("/stopped", "Stop service"),
                createCommand("/balance", "Return current balance"),
                createCommand("/price", "Return current price ")
        );
    }

    private static BotCommand createCommand(String commandName, String commandDescription) {
        return BotCommand.builder()
                .command(commandName)
                .description(commandDescription)
                .build();
    }
}
