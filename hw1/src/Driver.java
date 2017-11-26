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
	private SumCompiler compiler;
	

	public Driver() {
		// TODO Auto-generated constructor stub
	}
	
	public void run(){
		Scanner scan = new Scanner(System.in);
		System.out.println("Welcome to Kunhuan's hw1 version 5.0!\n");
		System.out.println("If your file is not under the working directory, type 1. Otherwise, enter your file.");
		String user_name = scan.nextLine();
		if (user_name.equals("1")){
			System.out.println("Enter the location url in the correct format.");
			String user_loc = scan.nextLine();
			Driver.locationURL=user_loc;
			System.out.println("Enter the name of your file then (without file extension).If you already typed the name of your file, too bad!\n Read intruction carefully next time.\n However, talented coder says it's ok. Just enter nothing next.");
			user_name=scan.nextLine();
		}
		scan.close();
		//find the file and read it
		if (user_name.endsWith(".sum")){
			System.out.println("I saw you put file extension within your file name. No worries. Program will still run.");
			Driver.fileName=user_name.replaceFirst("\\.sum", "");
		}
		else if (user_name.isEmpty()){
			System.out.println("I saw you typed file name when location was required. Trying to parse...");
			String[] arr = Driver.locationURL.split("(?=(\\w+\\.sum))");
			if (arr.length!=2){
				System.err.println("something's wrong with parsing the location.Sorry. Please follow the instruction next time!");
				System.exit(1);
			}
			else{
				Driver.locationURL=arr[0];
				Driver.fileName=arr[1];
			}
		}
		else{
		Driver.fileName=user_name;}
		File sumF = new File(Driver.locationURL+Driver.fileName);
		//System.out.println("file name :   " + sumF.toString());
		if (!sumF.canRead()){
			System.err.println("Sorry, but I did not find the file. The file location you wanted is: "+locationURL+"\n and your fileName is: "+Driver.fileName);
			System.exit(1);
		}
		compiler = new SumCompiler(Driver.locationURL, Driver.fileName);
		System.out.println("I've found the file. I'm gonna compile.");
		compiler.compile(sumF);

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
