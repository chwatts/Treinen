package chris.domain.treinpositie;

public sealed interface TreinPositie permits Onderweg, OpStation, OpStationMaarStoptNiet, EindBestemmingBereikt {}

