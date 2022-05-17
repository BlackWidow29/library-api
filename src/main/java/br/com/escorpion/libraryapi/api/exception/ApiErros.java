package br.com.escorpion.libraryapi.api.exception;

import br.com.escorpion.libraryapi.exception.BusinessException;
import org.springframework.validation.BindingResult;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

public class ApiErros {

    private final List<String> errors;

    public ApiErros(BindingResult bindingResult){
        this.errors = new ArrayList<>();
        bindingResult.getAllErrors().forEach(error -> this.errors.add(error.getDefaultMessage()));
    }

    public ApiErros(BusinessException exception) {
        this.errors = List.of(exception.getMessage());
    }

    public ApiErros(ResponseStatusException exception) {
        this.errors = List.of(exception.getReason());
    }

    public ApiErros(IllegalArgumentException exception) {
        this.errors = List.of(exception.getMessage());
    }

    public List<String> getErrors() {
        return errors;
    }
}
