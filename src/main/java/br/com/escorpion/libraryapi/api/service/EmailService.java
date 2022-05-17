package br.com.escorpion.libraryapi.api.service;


import java.util.List;

public interface EmailService {
    void sendEmails(List<String> mailsList, String message);
}
