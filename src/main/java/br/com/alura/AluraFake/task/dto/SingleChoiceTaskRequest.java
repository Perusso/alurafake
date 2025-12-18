package br.com.alura.AluraFake.task.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class SingleChoiceTaskRequest extends TaskRequest {

    @NotNull
    @Size(min = 2, max = 5)
    private List<OptionRequest> options;

    public List<OptionRequest> getOptions() { return options; }

    public void setOptions(List<OptionRequest> options) { this.options = options; }
}
