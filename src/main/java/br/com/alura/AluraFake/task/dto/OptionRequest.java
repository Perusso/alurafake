package br.com.alura.AluraFake.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class OptionRequest {

    @NotBlank
    @Size(min = 4, max = 80)
    private String option;

    @NotNull
    private Boolean isCorrect;


    public String getOption() { return option; }

    public void setOption(String option) { this.option = option; }

    public Boolean getIsCorrect() { return isCorrect; }

    public void setIsCorrect(Boolean correct) { isCorrect = correct; }
}
