package io.proj3ct.StatementBot.models;

import lombok.Getter;
@Getter
public class Transaction {

        private String date;
        private String time;
        private String authCode;
        private String category;
        private String merchant;
        private double amount;

        public Transaction(String date, String time, String authCode, String category, String merchant, double amount) {
            this.date = date;
            this.time = time;
            this.authCode = authCode;
            this.category = category;
            this.merchant = merchant;
            this.amount = amount;
        }

        @Override
        public String toString() {
            return String.format("Date: %s, Time: %s, Auth: %s, Category: %s, Merchant: %s, Amount: %.2f",
                    date, time, authCode, category, merchant, amount);
        }

    }

