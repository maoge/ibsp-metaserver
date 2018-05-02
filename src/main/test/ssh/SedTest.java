package ssh;

public class SedTest {

	public static void main(String[] args) {
		String src = "/home/ultravirs";
		String des = src.replaceAll("/", "\\\\/");
		
		System.out.println(des);
	}

}
