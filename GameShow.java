/* Name: Uyen-Dung Vo
 * UIN: 424008044
 * Acknowledgments: N/A
 */

import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.Timer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class GameShow {
	private static Lock lock = new ReentrantLock(); //for the lock.unlock() at the finally of PrintQ
	private static int timeFlag;
	private static boolean answered; 
	private static Condition answer; 
	private static Condition category;
	private static boolean intQuit; //interrupted quit
	
	//arraylists parallel
	private static int userScores[];

	//answer lists
	private static ArrayList<String> ans_1 = new ArrayList();
	private static ArrayList<String> ans_2 = new ArrayList();
	private static ArrayList<String> ans_3 = new ArrayList();
	private static ArrayList<String> ans_4 = new ArrayList();
	
	//category question list
	private static ArrayList<String> cat_1 = new ArrayList(); //Politics
	private static ArrayList<String> cat_2 = new ArrayList(); //TV Characters
	private static ArrayList<String> cat_3 = new ArrayList(); //TV Shows
	private static ArrayList<String> cat_4 = new ArrayList(); //KANYE WEST

	//game info
	private static int curPlayer;
	private static int numPlayers;
	private static ArrayList<String> catNames = new ArrayList();
	private static ArrayList<ArrayList<String>> cat = new ArrayList();
	private static ArrayList<ArrayList<String>> ans = new ArrayList();

	
	public GameShow(){
		//constructor to initialize
		timeFlag = 0;
		answered = false;
		answer = lock.newCondition();
		category = lock.newCondition();
		intQuit = false;
		
		//Politics
		cat_1.add("The orange man.");
		cat_1.add("_____ 2020?");
		ans_1.add("Donald Trump");
		ans_1.add("Kanye");
		
		//TV characters
		cat_2.add("Who is the one who knocks?");
		cat_2.add("Who is originally based on Larry David?");
		ans_2.add("Walter White");
		ans_2.add("George Costanza");
		
		//TV shows
		cat_3.add("\"What if you're right and they're wrong?\"");
		cat_3.add("\"Some people choose to see the ugliness in this world. The disarray. I choose to see the beauty.\"");
		ans_3.add("Fargo");
		ans_3.add("Westworld");
		
		//Kanye West
		cat_4.add("Who did Kanye West want to win the 2009 \"Best Female Video\" award?");
		cat_4.add("What is Kanye West's most crtically acclaimed album?");
		cat_4.add("Who would have Kanye voted for if he had voted?");
		cat_4.add("Who was Kanye's first fan?");
		ans_4.add("Beyonce");
		ans_4.add("My Beautiful Dark Twisted Fantasy");
		ans_4.add("Donald Trump");
		ans_4.add("Donda West");
		
		numPlayers = 0;
		curPlayer = 0;
		
		//category names
		catNames.add("Politics");
		catNames.add("TV Characters");
		catNames.add("TV Shows");
		catNames.add("Kanye West");
		
		cat.add(cat_1);
		cat.add(cat_2);
		cat.add(cat_3);
		cat.add(cat_4);
		ans.add(ans_1);
		ans.add(ans_2);
		ans.add(ans_3);
		ans.add(ans_4);
		
	}

	public static void main(String[] args) {

		Scanner sc = new Scanner(System.in);
		GameShow g = new GameShow();
		System.out.println("Welcome to the game. Enter \"X\" to quit a category at any time. Full names for points, +1 per correct answer.");
		System.out.println("How many players do we have?");
		numPlayers = sc.nextInt();
		userScores = new int [numPlayers];
		for(int i = 0; i<numPlayers; i++){
			userScores[i] = 0; //initialize points
		}
		
		Runnable gameStart = new GameStart();
		Thread threadG = new Thread(gameStart);
		threadG.start();
		//menu
		
		    

	}
	
	public static class GameStart implements Runnable{
		private Scanner sc = new Scanner(System.in);
		
		@Override
		public void run() {
			lock.lock();
			try{
				int cp = 0;
				while(!cat_1.isEmpty() || !cat_2.isEmpty() || !cat_3.isEmpty()){
					System.out.println("\nOkay, Player " + ((curPlayer=(cp++)%numPlayers)+1) + ", which category would you like to choose?");
					for(int i = 0; i<catNames.size(); i++){
						System.out.printf("%2d. %-20s\n",  i + 1, catNames.get(i));
					}
					
					int choice = sc.nextInt() - 1;
					System.out.println("");
					//create task
					Runnable printQuestion = new PrintQ(cat.get(choice), ans.get(choice));
					cat.remove(choice);
					ans.remove(choice);
					catNames.remove(choice);
					// Create threads
					Thread threadP = new Thread(printQuestion);	

					// Start threads
					threadP.start();
					category.await();
				}
				
				//game ends when we get through all the questions
				System.out.println("\nGame end!");
				System.out.printf("%s %-5s\n", "Player", "Score");
				System.out.println("--------------------");

				for(int i = 0; i<userScores.length; i++){
					System.out.printf("%d %-5d\n", (i+1), userScores[i] );
	
				}
				
				if(!intQuit){
					System.out.println("\nWould you like to play again? (\"y\" for yes)");
					sc.nextLine(); //buffer
					if(sc.nextLine().toLowerCase().equals("y")){
						//call the main method of the "next" game instance
						System.out.println("");
						String[] args={};
						GameShow.main(args);
					}
				}
			
				else{
					sc.close(); //close the scanner
				}
			} catch (Exception e){
				e.printStackTrace();
			}
			finally{
				lock.unlock();
			}
			
		}
	}
	
	//helper
	private static boolean isNumeric(String s) {  
	    return s.matches("[-+]?\\d*\\.?\\d+");  
	}  
	
	public static class PrintQ implements Runnable{
		private Scanner sc = new Scanner(System.in);
		private String userAns;
		private int newQ;
		private int numQ;
		ArrayList<String> pickedQ = new ArrayList();
		ArrayList<String> pickedA = new ArrayList();

		public PrintQ(ArrayList<String> p, ArrayList<String> a){
			userAns = "";
			newQ = p.size();
			numQ = a.size();
			pickedQ = p;
			pickedA = a;
		}
		
		
		@Override
		public void run() {
			lock.lock();
			try{
				
				for(int i = 0; i<numQ; i++){
					int pick = (int)(Math.random() * newQ);
					newQ--;		
					System.out.println("Current points: " + userScores[curPlayer]);
					System.out.println(pickedQ.get(pick));
					pickedQ.remove(pick); //randomize cat_1
					timeFlag = 0;
					answered = false;
					Runnable timer = new TimeTen();
					Thread threadT = new Thread(timer);
				    threadT.start();
				    
					userAns = sc.nextLine(); 
				    if(!isNumeric(userAns)){ //toLowerCase seems to mess up numeric strings
				    	String temp = userAns.toLowerCase(); // case support
				    	userAns = temp;
				    }
				    
					if(userAns.equals("X") || userAns.equals("x")){
						threadT.interrupt(); //stop the timer
						intQuit = true;
						break;						
					}
					
				    Runnable reader = new Read(pick, userAns, pickedA);
				    Thread threadR = new Thread(reader);
				    threadR.start();
				    answer.await();
				    if(timeFlag == 1){
						continue;
					}
				    threadT.interrupt(); //I do this so the timer does not overlap into the next question
				    					//This is on purpose and my way to get around using Thread.sleep instead of Timer
				    
				}

				System.out.println("Current points: " + userScores[curPlayer]);
				category.signalAll();
//				System.out.println("Game end! Total points: " + points);
//				
//				if(!intQuit){
//					System.out.println("Would you like to play again? (\"y\" for yes)");
//					String a = sc.nextLine();
//					if(a.equals("y") || a.equals("Y")){
//						//call the main method of the "next" game instance
//						String[] args={};
//						GameShow.main(args);
//						System.out.println("");
//					}
//				}
			
			//	else{
					//sc.close(); //close the scanner
				//}
				
			//close of the current instance of the game

			} catch (Exception e){
				e.printStackTrace();
			}
			finally{
				lock.unlock();
			}
		}
			
	}
	  
	public static class Read implements Runnable{
		String trueAnsLOW;
		String trueAns;
		String userAns;
		
		public Read(int ansIndex, String userAns, ArrayList<String> a){
			trueAns = a.get(ansIndex);
			if(!isNumeric(trueAns)){ //toLowerCase seems to mess up numeric strings
		    	String temp = trueAns.toLowerCase(); // case support
		    	trueAnsLOW = temp;
		    }
			else{
				//if it is numeric then it is the same, no lower case format
				trueAnsLOW = trueAns;
			}
			a.remove(ansIndex);
			this.userAns = userAns;

		}
		
		public void run() {
			lock.lock();
			//read in answer
			try{
				if(timeFlag == 0){
					if(userAns.compareTo(trueAnsLOW)==0){
						userScores[curPlayer]++;
						System.out.println("Correct!\n");
					}
					else{
						System.out.println("Wrong. Correct answer is: "+trueAns+"\n");
					}
					
				}
				else{
					System.out.println("Correct answer is: "+trueAns+"\n");
				}
				answered = true;
				answer.signalAll();
			} catch(Exception e) {
				e.printStackTrace();
			}
			finally{
				lock.unlock();
			}
		}
		  
	  }
	  
	  public static class TimeTen implements Runnable{
		  
		@Override
		public void run() {
			try {
				Thread.sleep(10000);
				if(!answered){
					System.out.println("Timeout. Press enter to reveal answer and to prompt next question.");
					timeFlag = 1;
				}
				
			} catch (InterruptedException e) {
				System.out.print("");
			}
		}
		  
	  }
}


