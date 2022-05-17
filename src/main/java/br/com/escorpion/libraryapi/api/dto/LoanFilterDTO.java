package br.com.escorpion.libraryapi.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoanFilterDTO {

    private String isbn;
    private String customer;

}
