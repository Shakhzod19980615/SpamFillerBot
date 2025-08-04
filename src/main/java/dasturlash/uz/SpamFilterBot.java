package dasturlash.uz;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.BanChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Component
public class SpamFilterBot extends TelegramLongPollingBot {

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

    private final List<String> bannedWords = List.of(
            "sex", "porn", "nude", "xxx", "18+", "click here", "hot girl");

    @Override
    public void onUpdateReceived(Update update) {
        System.out.println("Update received: " + update.toString());
        if (!update.hasMessage()) return;

        Message message = update.getMessage();
        String text = message.getText();
        Long chatId = message.getChatId();
        System.out.println("Message received in chat: " + chatId);
        System.out.println("Message text: " + text);

        if (text != null && containsBannedWords(text)) {
            deleteMessage(chatId, message.getMessageId());
            banUser(chatId, message.getFrom().getId());
            sendWarning(chatId, message.getFrom().getFirstName());
        }
    }

    private boolean containsBannedWords(String text) {
        return bannedWords.stream().anyMatch(word ->
                text.toLowerCase().contains(word)
        );
    }

    private void deleteMessage(Long chatId, Integer messageId) {
        DeleteMessage delete = new DeleteMessage(chatId.toString(), messageId);
        try {
            execute(delete);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void banUser(Long chatId, Long userId) {
        BanChatMember ban = new BanChatMember();
        ban.setChatId(chatId.toString());
        ban.setUserId(userId);
        try {
            execute(ban);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendWarning(Long chatId, String user) {
        SendMessage warning = new SendMessage();
        warning.setChatId(chatId.toString());
        warning.setText("🚫 User " + user + " was banned for violating chat rules.");
        try {
            execute(warning);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }
}
