package br.com.portfolio.hospital.shared.exception;

/** Vira HTTP 422: o dado é bem formado, mas viola uma regra do negócio. */
public class RegraDeNegocioException extends RuntimeException {
    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
