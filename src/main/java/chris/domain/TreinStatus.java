package chris.domain;

import org.jspecify.annotations.NonNull;

public record TreinStatus(@NonNull Trein trein, @NonNull TreinPositie treinPositie, int minutenVertraging) {
    @Override
    public String toString() {
        return trein + " nu hier: " + treinPositie;
    }
}
