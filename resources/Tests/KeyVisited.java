import ist.meic.pa.KeywordArgs;

public class KeyVisited extends KeyPlaces {
	int visited;

	@KeywordArgs("visited=0,second")
	public KeyVisited(Object... args) {}

	public String toString() {
		return String.format("visited: %s, places: %s, %s, %s",
				visited, first, second, third);
	}

}
