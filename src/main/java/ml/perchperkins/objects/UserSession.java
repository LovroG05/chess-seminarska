package ml.perchperkins.objects;

import java.util.UUID;

public record UserSession(
        UUID uuid,
        boolean isWhite
) {
}
