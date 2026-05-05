package chris.domain;

public sealed interface TreinPositie permits Onderweg, OpStation, OpStationMaarStoptNiet, EindBestemmingBereikt {}

