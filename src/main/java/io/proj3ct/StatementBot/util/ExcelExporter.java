package io.proj3ct.StatementBot.util;

import io.proj3ct.StatementBot.models.Transaction;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExcelExporter {

    public static void exportToExcel(List<Transaction> transactions, String outputPath) {
        try (Workbook workbook = new XSSFWorkbook()) {
            // Создаем стиль для заголовков
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // Лист со всеми транзакциями
            Sheet transactionsSheet = workbook.createSheet("Транзакции");
            createTransactionsSheet(transactionsSheet, transactions, headerStyle);

            // Лист со статистикой по категориям
            Sheet categoriesSheet = workbook.createSheet("Статистика по категориям");
            createCategoryStatistics(categoriesSheet, transactions, headerStyle);

            // Лист со статистикой по мерчантам
            Sheet merchantsSheet = workbook.createSheet("Статистика по мерчантам");
            createMerchantStatistics(merchantsSheet, transactions, headerStyle);

            // Лист с общей статистикой
            Sheet summarySheet = workbook.createSheet("Общая статистика");
            createSummaryStatistics(summarySheet, transactions, headerStyle);

            // Автоматическая настройка ширины столбцов
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                for (int j = 0; j < 10; j++) {
                    sheet.autoSizeColumn(j);
                }
            }

            // Сохраняем файл
            try (FileOutputStream outputStream = new FileOutputStream(outputPath)) {
                workbook.write(outputStream);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void createTransactionsSheet(Sheet sheet, List<Transaction> transactions, CellStyle headerStyle) {
        // Создаем заголовки
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Дата", "Мерчант", "Категория", "Сумма"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Заполняем данными
        int rowNum = 1;
        for (Transaction transaction : transactions) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(transaction.getDate().toString());
            row.createCell(1).setCellValue(transaction.getMerchant());
            row.createCell(2).setCellValue(transaction.getCategory());
            row.createCell(3).setCellValue(transaction.getAmount());
        }
    }

    private static void createCategoryStatistics(Sheet sheet, List<Transaction> transactions, CellStyle headerStyle) {
        // Создаем заголовки
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Категория", "Сумма"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Группируем данные по категориям
        Map<String, Double> categoryStats = transactions.stream()
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.summingDouble(Transaction::getAmount)
                ));

        // Заполняем данными
        int rowNum = 1;
        for (Map.Entry<String, Double> entry : categoryStats.entrySet()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(entry.getKey());
            row.createCell(1).setCellValue(entry.getValue());
        }
    }

    private static void createMerchantStatistics(Sheet sheet, List<Transaction> transactions, CellStyle headerStyle) {
        // Создаем заголовки
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Мерчант", "Сумма"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Группируем данные по мерчантам
        Map<String, Double> merchantStats = transactions.stream()
                .collect(Collectors.groupingBy(
                        Transaction::getMerchant,
                        Collectors.summingDouble(Transaction::getAmount)
                ));

        // Заполняем данными
        int rowNum = 1;
        for (Map.Entry<String, Double> entry : merchantStats.entrySet()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(entry.getKey());
            row.createCell(1).setCellValue(entry.getValue());
        }
    }

    private static void createSummaryStatistics(Sheet sheet, List<Transaction> transactions, CellStyle headerStyle) {
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Общая статистика");
        headerRow.getCell(0).setCellStyle(headerStyle);

        Row totalRow = sheet.createRow(1);
        totalRow.createCell(0).setCellValue("Общая сумма расходов:");
        totalRow.createCell(1).setCellValue(
                transactions.stream()
                        .mapToDouble(Transaction::getAmount)
                        .sum()
        );
    }
}