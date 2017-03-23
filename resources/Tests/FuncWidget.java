import ist.meic.pa.KeywordArgs;

class FuncWidget {
	double result;
	double value;

	@KeywordArgs("result=40+5,value=Math.sin(result)")
	public FuncWidget(Object... args) {}

	public String toString() {
		return String.format("result:%s,value:%s",
				result, value);
	}
}