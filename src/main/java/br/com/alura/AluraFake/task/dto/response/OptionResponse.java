package br.com.alura.AluraFake.task.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OptionResponse {
    private String option;

    @JsonProperty("isCorrect")
    private Boolean correct;

    public OptionResponse() {
    }

    public OptionResponse(String option, Boolean correct) {
        this.option = option;
        this.correct = correct;
    }

    public String getOption() { return option; }

    public Boolean isCorrect() { return correct; }
}
