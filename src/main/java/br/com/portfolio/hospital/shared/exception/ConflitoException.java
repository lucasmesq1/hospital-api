package br.com.portfolio.hospital.shared.exception;

/** Vira HTTP 409: a operação conflita com o estado atual (duplicidade, leito ocupado...). */
public class ConflitoException extends RuntimeException {
    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
