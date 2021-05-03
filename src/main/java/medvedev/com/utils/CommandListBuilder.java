package medvedev.com.utils;

import lombok.experimental.UtilityClass;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;

import java.util.Arrays;
import java.util.List;

@UtilityClass
public class CommandListBuilder {

    public static List<BotCommand> getCommandList() {
        return Arrays.asList(
                createCommand("/inviolableresidue", "Set inviolable residue"),
                createCommand("/minuteswithoutchange", "Set minutes count without change"),
                createCommand("/price", "Return current price "),
                createCommand("/balance", "Return current balance"),
                createCommand("/fiatcrypt", "Set fiat-crypt percent difference"),
                createCommand("/cryptfiat", "Set crypt-fiat percent difference"),
                createCommand("/launched", "Launch service"),
                createCommand("/stopped", "Stop service"),
                createCommand("/start", "Authenticated command")
        );
    }

    private static BotCommand createCommand(String commandName, String commandDescription) {
        return BotCommand.builder()
                .command(commandName)
                .description(commandDescription)
                .build();
    }
}
