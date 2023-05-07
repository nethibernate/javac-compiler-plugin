public class Test {

    public static void main(String[] args) {
        new Test().test();
    }

    public void test() {
		a();
        System.out.println("Original Method!");
    }
	
	private void a(){
		System.out.println("In a");
	}
}
