import ist.meic.pa.KeywordArgs;

class ExtendedWidget extends Widget {
	String name;

	@KeywordArgs("name=\"Extended\",width=200,margin=10")
	public ExtendedWidget(Object... args) {}

	public String toString() {
		return String.format("width:%s,height:%s,margin:%s,name:%s",
				width, height, margin, name);
	}
}
