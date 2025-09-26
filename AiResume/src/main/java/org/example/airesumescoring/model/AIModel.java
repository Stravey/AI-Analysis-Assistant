package org.example.airesumescoring.model;

public enum AIModel {
    DEEPSEEK("deepseek"),
    DOUBAO("doubao");

    private final String value;

    AIModel(String value) {
        this.value = value;
    }

    public static AIModel fromString(String text) {
        for (AIModel model : AIModel.values()) {
            if (model.value.equalsIgnoreCase(text)) {
                return model;
            }
        }
        throw new IllegalArgumentException("No constant with text " + text + " found");
    }

    public String getValue() {
        return value;
    }
}
