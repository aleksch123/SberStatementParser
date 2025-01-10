package io.proj3ct.StatementBot.service;

import io.proj3ct.StatementBot.config.BotConfig;
import io.proj3ct.StatementBot.models.Transaction;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static io.proj3ct.StatementBot.handler.ExcelExporter.exportToExcel;
import static io.proj3ct.StatementBot.handler.PDFTransactionParser.parseTransactions;

//@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    final BotConfig config;

    public TelegramBot(BotConfig config){
        this.config=config;
    };
    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage()&& update.getMessage().hasText()){

            String messageText = update.getMessage().getText();
            long chatId =update.getMessage().getChatId();
            switch (messageText){
                case "/start":
                    startCommandReceived(String.valueOf(chatId),update.getMessage().getChat().getFirstName());
                    break;
                default: sendMessage(String.valueOf(chatId),"Sorry command was not recognized");
            }

        }

        if (update.hasMessage() && update.getMessage().hasDocument()) {
            Message message = update.getMessage();
            Document document = message.getDocument();
            String chatId = message.getChatId().toString();

            try {
                // Получаем информацию о файле
                GetFile getFile = new GetFile();
                getFile.setFileId(document.getFileId());
                org.telegram.telegrambots.meta.api.objects.File telegramFile = execute(getFile);

                // Загружаем файл
                String filePath =downloadFile(telegramFile).getPath();
                File downloadedFile = new File(filePath);

                // Обработка файла
                List<Transaction> transactions = parseTransactions(filePath);

                //Экспортируем данные в Excel
                exportToExcel(transactions,"statement.xlsx");


                // Отправляем подтверждение пользователю
                SendMessage response = new SendMessage();
                response.setChatId(chatId);
                response.setText("Файл " + document.getFileName() + " успешно получен и обработан!");
                execute(response);
                sendFile(chatId,"statement.xlsx");

                // Удаляем временный файл
                Files.deleteIfExists(downloadedFile.toPath());

            } catch (TelegramApiException | IOException e) {
                SendMessage errorMessage = new SendMessage();
                errorMessage.setChatId(chatId);
                errorMessage.setText("Произошла ошибка при обработке файла: " + e.getMessage());
                try {
                    execute(errorMessage);
                } catch (TelegramApiException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }



    private void startCommandReceived(String chatId, String firstName){
        String answer ="Hi ,"+firstName+", nice to meet you";
        sendMessage(chatId,answer);
    }

    private void sendMessage(String chatId,String textToSend){
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(textToSend);
        try {
            execute(message);
        }
        catch (TelegramApiException e){
            throw new RuntimeException();
        }
    }

    private void sendFile(String chatId,String filePath) {
        try {
            // Создаем файл для отправки
            File file = new File(filePath); // Укажите путь к вашему файлу

            // Метод 1: Отправка файла с локального диска
            SendDocument document = new SendDocument();
            document.setChatId(chatId);
            document.setDocument(new InputFile(file));
            document.setCaption("Вот ваша выписка!"); // Опционально: подпись к файлу
            execute(document);

        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

}
