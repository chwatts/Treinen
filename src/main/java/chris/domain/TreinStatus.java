package chris.domain;

import chris.domain.treinpositie.TreinPositie;
import org.jspecify.annotations.NonNull;

public record TreinStatus(@NonNull Trein trein, @NonNull TreinPositie treinPositie, int minutenVertraging) {
    @Override
    @NonNull
    public String toString() {
        return trein + " nu hier: " + treinPositie;
    }
}
