package com.gdlatte.generator.entity.vo;

import lombok.Getter;

@Getter
public enum FileExtension {
    //
    JAVA(".java"),
    PROPERTIES(".properties"),
    YML(".yml"),
    YAML(".yaml"),
    GRADLE(".gradle"),
    VIZEND(".vizend"),
    JSON(".json"),
    TS(".ts"),
    JS(".js"),
    VUE(".vue"),
    TSX(".tsx"),
    JSX(".jsx"),
    ;

    private final String value;

    FileExtension(String value) {
        this.value = value;
    }

    public static boolean hasValidExtension(String fileName) {
        //
        for (FileExtension extension : FileExtension.values()) {
            if (fileName.endsWith(extension.getValue())) {
                return true;
            }
        }
        return false;
    }

    public static String getLanguageIdentifier(FileExtension fileExtension) {
        //
        return switch (fileExtension) {
            case JAVA, GRADLE -> "java";
            case PROPERTIES, YML, YAML -> "yml";
            case VIZEND ->  "text";
            case JSON -> "json";
            case VUE -> "vue";
            case TSX, JSX, TS, JS -> "typescript";
        };
    }
    public static FileExtension fromExtension(String extension) {
        for (FileExtension ext : FileExtension.values()) {
            if (ext.getValue().equals(extension)) {
                return ext;
            }
        }
        throw new IllegalArgumentException("No enum constant for extension: " + extension);
    }
}

