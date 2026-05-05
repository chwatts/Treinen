package chris.domain;

import org.jspecify.annotations.NonNull;

public record OpStation(@NonNull Station station) implements TreinPositie{
}
