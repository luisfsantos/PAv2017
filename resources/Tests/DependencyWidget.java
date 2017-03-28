import ist.meic.pa.KeywordArgs;

class DependencyWidget {
	double a;
	double b;
	double c;

	public DependencyWidget() {
		a=0;
	}

	@KeywordArgs("b=a+5,a=4,c=a*a")
	public DependencyWidget(Object... args) {}

	public String toString() {
		return String.format("a:%s,b:%s,c:%s",
				a, b, c);
	}
}