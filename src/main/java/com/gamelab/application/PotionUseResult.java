package com.gamelab.application;

public record PotionUseResult(
        boolean success,
        String message
) {

    public static PotionUseResult successful(
            String message
    ) {
        return new PotionUseResult(true, message);
    }

    public static PotionUseResult failed(
            String message
    ) {
        return new PotionUseResult(false, message);
    }
}