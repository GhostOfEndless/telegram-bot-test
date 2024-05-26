package ru.synthiq.configuration;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updates.DeleteWebhook;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.telegram.telegrambots.webhook.TelegramWebhookBot;
import org.telegram.telegrambots.webhook.starter.TelegramBotsSpringWebhookApplication;

@Slf4j
@Service
public class TelegramBot implements TelegramWebhookBot {

    @Value("${telegram.bot.uri}")
    private String botUri;
    @Value("${telegram.bot.path}")
    private String botPath;

    private final TelegramBotsSpringWebhookApplication telegramBotsSpringWebhookApplication;
    private final TelegramClient telegramClient;

    public TelegramBot(TelegramBotsSpringWebhookApplication telegramBotsSpringWebhookApplication,
                       @Value("${telegram.bot.token}") String botToken) {
        this.telegramBotsSpringWebhookApplication = telegramBotsSpringWebhookApplication;
        this.telegramClient = new OkHttpTelegramClient(botToken);
    }

    @PostConstruct
    public void init() {
        try {
            telegramBotsSpringWebhookApplication.registerBot(
                    botPath,
                    this::consumeUpdate,
                    this::runSetWebhook,
                    this::runDeleteWebhook
            );
        } catch (TelegramApiException e) {
            log.error("Error registering bot!");
        }
    }

    @Override
    public void runDeleteWebhook() {
        try {
            telegramClient.execute(new DeleteWebhook());
        } catch (TelegramApiException e) {
            log.info("Error deleting webhook!");
        }
    }

    @Override
    public void runSetWebhook() {
        try {
            telegramClient.execute(SetWebhook
                    .builder()
                    .url(botUri + "/" + getBotPath())
                    .build());
        } catch (TelegramApiException e) {
            log.info("Error setting webhook!");
        }
    }

    @Override
    public BotApiMethod<?> consumeUpdate(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            return SendMessage
                    .builder()
                    .chatId(update.getMessage().getChatId().toString())
                    .text("Test message")
                    .build();
        }
        return null;
    }

    @Override
    public String getBotPath() {
        return botPath;
    }
}
