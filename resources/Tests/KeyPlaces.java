import ist.meic.pa.KeywordArgs;

public class KeyPlaces {
	String first;
	String second;
	String third;
	
	@KeywordArgs("first=\"Lordran\",second=\"Drangleic\",third=\"Lothric\"")
	public KeyPlaces(Object... args) {}

	public String toString() {
		return String.format("Interesting places: %s, %s, %s",
				first, second, third);
	}
}
