package mehedi.stockwatch.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(StockAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleStockAlreadyExists(
            StockAlreadyExistsException exception) {

        return Map.of(
                "error", "Stock already exists",
                "message", exception.getMessage()
        );
    }

    @ExceptionHandler(StockNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleStockNotFound(
            StockNotFoundException exception) {

        return Map.of(
                "error", "Stock not found",
                "message", exception.getMessage()
        );
    }
}