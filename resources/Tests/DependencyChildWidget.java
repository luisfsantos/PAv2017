import ist.meic.pa.KeywordArgs;

class DependencyChildWidget extends DependencyWidget {
	double d;

	@KeywordArgs("a=c,c=d,d")
	public DependencyChildWidget(Object... args) {}

	public String toString() {
		return String.format("a:%s,b:%s,c:%s,d:%s",
				a, b, c, d);
	}
}