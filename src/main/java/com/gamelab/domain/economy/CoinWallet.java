package com.gamelab.domain.economy;

public final class CoinWallet {

    private int balance;

    public CoinWallet() {
        this(0);
    }

    public CoinWallet(int initialBalance) {
        if (initialBalance < 0) {
            throw new IllegalArgumentException(
                    "Initial balance cannot be negative"
            );
        }

        this.balance = initialBalance;
    }

    public void addCoins(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException(
                    "Coin amount must be positive"
            );
        }

        balance += amount;
    }

    public boolean spendCoins(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException(
                    "Coin amount must be positive"
            );
        }

        if (balance < amount) {
            return false;
        }

        balance -= amount;
        return true;
    }

    public boolean canAfford(int amount) {
        if (amount < 0) {
            return false;
        }

        return balance >= amount;
    }

    public int getBalance() {
        return balance;
    }
}