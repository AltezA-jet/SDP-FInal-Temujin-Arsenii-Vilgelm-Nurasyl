package com.gamelab.domain.shop;

public record PurchaseResult(
        boolean success,
        String message
) {

    public static PurchaseResult successful(
            String message
    ) {
        return new PurchaseResult(true, message);
    }

    public static PurchaseResult failed(
            String message
    ) {
        return new PurchaseResult(false, message);
    }
}