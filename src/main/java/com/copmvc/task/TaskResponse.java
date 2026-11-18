package com.copmvc.task;

public record TaskResponse(Long id, String title, String description, boolean completed) {
}
