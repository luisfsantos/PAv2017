public class TestI {
	public static void main(String[] args) {
		System.err.println(new DependencyChildWidget());
		System.err.println(new DependencyWidget());
		System.err.println(new DependencyWidget("a", 6));
		System.err.println(new DependencyChildWidget("a", 6));
	}
}
