package br.com.pedidos.apiPedidos.adapters.entrada.rest;

import br.com.pedidos.apiPedidos.application.pedido.ItensObrigatoriosException;
import br.com.pedidos.apiPedidos.domain.pedido.ItemInvalidoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class PedidoExceptionHandler {

    @ExceptionHandler(ItemInvalidoException.class)
    public ResponseEntity<Map<String, String>> itemInvalido(ItemInvalidoException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(ItensObrigatoriosException.class)
    public ResponseEntity<Map<String, String>> itensObrigatorios(ItensObrigatoriosException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> camposInvalidos(MethodArgumentNotValidException ex) {
        String mensagem = ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> erro.getField() + " " + erro.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("dados inválidos");
        return ResponseEntity.badRequest().body(Map.of("message", mensagem));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> jsonInvalido(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(Map.of("message", "JSON malformado"));
    }
}
