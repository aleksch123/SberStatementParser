package io.proj3ct.StatementBot.handler;

import io.proj3ct.StatementBot.models.Transaction;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PDFTransactionParser {
    private static final Pattern TRANSACTION_PATTERN = Pattern.compile(
            "(\\d{2}\\.\\d{2}\\.\\d{4})\\s+(\\d{2}:\\d{2})\\s+(\\d{6})\\s+" +
                    "(Прочие расходы|Рестораны и кафе|Супермаркеты|Все для дома|Здоровье и красота|" +
                    "Автомобиль|Одежда и аксессуары|Отдых и развлечения|Транспорт|Прочие операции)\\s+" +
                    "(\\d+[\\s\\u00A0]*\\d*,\\d{2})"
    );

    public static List<Transaction> parseTransactions(String pdfPath) {
        List<Transaction> transactions = new ArrayList<>();

        try (PDDocument document = PDDocument.load(new File(pdfPath))) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            String[] lines = text.split("\n");
            String currentMerchant = "";
            for (int i = 0; i < lines.length; i++) {
                Matcher matcher = TRANSACTION_PATTERN.matcher(lines[i]);

                if (matcher.find()) {
                    // Ищем название мерчанта в следующей строке
                    if (i + 1 < lines.length) {
                        currentMerchant = lines[i + 1].trim().substring(11);
                        // Пропускаем строки, которые похожи на даты или суммы
//                        if (currentMerchant.matches(".*\\d{2}\\.\\d{2}\\.\\d{4}.*") ||
//                                currentMerchant.matches(".*\\d+,\\d{2}.*")) {
//                            continue;
//                        }
                    }

                    // Извлекаем данные из текущей строки
                    String date = matcher.group(1);
                    String time = matcher.group(2);
                    String authCode = matcher.group(3);
                    String category = matcher.group(4);

                    // Преобразуем сумму из строки в double
                    String amountStr = matcher.group(5)
                            .replace(" ", "")
                            .replace("\u00A0", "")
                            .replace(",", ".");
                    double amount = Double.parseDouble(amountStr);

                    // Создаем новую транзакцию
                    transactions.add(new Transaction(
                            date, time, authCode, category, currentMerchant, amount
                    ));
                }
            }

        } catch (IOException e) {
            System.err.println("Ошибка при чтении PDF файла: " + e.getMessage());
            e.printStackTrace();
        }

        return transactions;
    }

    public static void parse(String pdfPath) {

        List<Transaction> transactions = parseTransactions(pdfPath);

        // Вывод всех транзакций
        System.out.println("Найденные транзакции:");
        transactions.forEach(System.out::println);

        // Вывод статистики по категориям
        System.out.println("\nСтатистика по категориям:");
        transactions.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        Transaction::getCategory,
                        java.util.stream.Collectors.summingDouble(Transaction::getAmount)
                ))
                .forEach((category, sum) ->
                        System.out.printf("%s: %.2f руб.%n", category, sum));

        System.out.println("\nСтатистика по мерчантам:");
        transactions.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        Transaction::getMerchant,
                        java.util.stream.Collectors.summingDouble(Transaction::getAmount)
                ))
                .forEach((merchant, sum) ->
                        System.out.printf("%s: %.2f руб.%n", merchant, sum));

        // Вывод общей суммы расходов
        double totalSpent = transactions.stream()
                .mapToDouble(Transaction::getAmount)
                .sum();
        System.out.printf("\nОбщая сумма расходов: %.2f руб.%n", totalSpent);
    }
}