package com.gamelab.application;

public record RuneActionResult(
        boolean success,
        String message
) {

    public static RuneActionResult successful(
            String message
    ) {
        return new RuneActionResult(
                true,
                message
        );
    }

    public static RuneActionResult failed(
            String message
    ) {
        return new RuneActionResult(
                false,
                message
        );
    }
}