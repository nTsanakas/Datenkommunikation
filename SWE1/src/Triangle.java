
public class Triangle {

	public static void main(String[] args) {
		int columns = In.readInt();

	    System.out.println(columns);

	    for (int i = 0; i < columns/2 + 1; i++) {
	      System.out.println(" ");
	      for (int j = 0; j < columns; j++) {
	        if (j < i || j > columns / 2 + 1) {
	          System.out.print(".");
	        }
	        else {
	          System.out.print("*");
	        }
	      }
	    }

	}

}
