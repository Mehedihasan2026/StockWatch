package mehedi.stockwatch.dto;

public record AuthUserResponse(
        Long id,
        String email,
        String displayName
) {
}