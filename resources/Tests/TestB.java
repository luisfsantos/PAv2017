public class TestB {
	public static void main(String[] args) {
		System.err.println(new ExtendedWidget());
		System.err.println(new ExtendedWidget("width", 80));
		System.err.println(new ExtendedWidget("height", 30));
		System.err.println(new ExtendedWidget("height", 20, "width", 90));
		System.err.println(new ExtendedWidget("height", 20, "width", 90, "name", "Nice"));
	}
}
