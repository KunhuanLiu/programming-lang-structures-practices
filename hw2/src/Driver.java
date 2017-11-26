import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Driver takes care of reading input file and other user interactions
 * Requirement:
 * 1.Assume that the name of the input file will end with 
 * and your output file should have the same basename with the extension
 */
public class Driver {
	// reader and output location
	private static String locationURL = "";
	private static String fileName;
	//private SumCompiler compiler;
	

	public Driver() {
		// TODO Auto-generated constructor stub
	}
	
	
	
	public void run(){

		
		Scanner scan = new Scanner(System.in);
		System.out.println("Welcome to Kunhuan's hw2!\n Please enter the file name. The file should be in the same folder as this running program.");
		String user_name = scan.nextLine();
		Driver.fileName = user_name;
		scan.close();
		File sumF = new File(Driver.fileName);
		//System.out.println("file name :   " + sumF.toString());
		if (!sumF.canRead()){
			System.err.println("Sorry, but I did not find the file. Working Dir: "+System.getProperty("user.dir")+" Your fileName is: "+Driver.fileName);
			System.exit(1);
		}
		Parser parser = new Parser(sumF);
		parser.run();

	}
	public static String getLocation(){
		return locationURL;
	}
	
	public static String getSumName(){
		return fileName;
	}
	
	public static void main(String[] args) {
		Driver driver = new Driver();
		driver.run();
	}

}
