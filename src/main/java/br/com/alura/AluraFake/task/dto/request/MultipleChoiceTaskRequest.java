package br.com.alura.AluraFake.task.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class MultipleChoiceTaskRequest extends TaskRequest {

    @NotNull
    private List<OptionRequest> options;

    public List<OptionRequest> getOptions() { return options; }
    public void setOptions(List<OptionRequest> options) { this.options = options; }
}
