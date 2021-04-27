package medvedev.com.service.security;

import lombok.RequiredArgsConstructor;
import medvedev.com.enums.ChatState;
import medvedev.com.repository.ChatStateRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatStateService {

    private final ChatStateRepository repository;

    public ChatState getStateByChat(Long idChat) {
        return repository.findByIdChat(idChat).map(entity -> entity.getState().getState()).orElse(ChatState.NONE);
    }

    public void updateChatState(Long idChat, ChatState state) {
        repository.findByIdChat(idChat).ifPresentOrElse(entity -> repository.changeStateByIdChat(idChat, state.name()),
                () -> repository.insertNewChatState(idChat, state.name()));
    }

    public List<Long> getAuthenticatedChats() {
        return repository.findAllByState(ChatState.AUTHENTICATED.name());
    }
}
