/* Name: Uyen-Dung Vo
 * UIN: 424008044
 * Acknowledgments: N/A
 */

import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.Timer;
import java.util.ArrayList;
import java.util.Scanner;

//add multiplayer
//add restart
//difficulty/levels? -> faster time
//static -> inner classes don't have access to private of outer class
//add random questions

public class GameShow {
	private static int points;
	private static Lock lock = new ReentrantLock(); //for the lock.unlock() at the finally of PrintQ
	private static int timeFlag;
	private static boolean answered; 
	private static Condition answer;
	private static boolean intQuit; //interrupted quit
	private static ArrayList<String> answers = new ArrayList();
	
	public GameShow(){
		//constructor to initialize
		points = 0;
		timeFlag = 0;
		answered = false;
		answer = lock.newCondition();
		intQuit = false;
	}

	public static void main(String[] args) {
		// Create tasks
		GameShow g = new GameShow();
		System.out.println("Welcome to the game. Enter \"X\" to quit at any time.");
		Runnable printQuestion = new PrintQ();
		//Runnable restart = new Restart();
		    
		// Create threads
		Thread threadP = new Thread(printQuestion);
		//Thread threadRS = new Thread(restart);
		    
		// Start threads
		threadP.start();

	}
	
//	public static class Restart implements Runnable{
//		
//		public Restart(){
//			while(gameStart){
//				if(restartFlag){
//					//calling the main method again
//					//garbage collection in Java is automatic
//				}
//			}
//		}
//		
//		@Override
//		public void run(){
//			
//		}
//	}
	
	private static boolean isNumeric(String s) {  
	    return s.matches("[-+]?\\d*\\.?\\d+");  
	}  
	
	public static class PrintQ implements Runnable{
			private ArrayList<String> questions = new ArrayList();
			private Scanner sc = new Scanner(System.in);
			private String userAns;
			private int newQ;
			private int numQ;

			public PrintQ(){
				questions.add("Who was the first president of the U.S.?");
				questions.add("What is Obama's first name?");
				questions.add("What is Professor Dutta's first name?");
				questions.add("What is 2+2? (Number)");
				answers.add("George Washington");
				answers.add("Barack");
				answers.add("Anandi");
				answers.add("4");
				userAns = "";
				newQ = questions.size();
				numQ = questions.size();
			}
			
			
			@Override
			public void run() {
				lock.lock();
				try{
				for(int i = 0; i<numQ; i++){
					int pick = (int)(Math.random() * newQ);
					newQ--;		
					System.out.println("Current points: " + points);
					System.out.println(questions.get(pick));
					questions.remove(pick); //randomize questions
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
					
				    Runnable reader = new Read(pick, userAns);
				    Thread threadR = new Thread(reader);
				    threadR.start();
				    answer.await();
				    if(timeFlag == 1){
						continue;
					}
				    threadT.interrupt(); //I do this so the timer does not overlap into the next question
				    					//This is on purpose and my way to get around using Thread.sleep instead of Timer
				    
				}
				System.out.println("Game end! Total points: " + points);
				
				if(!intQuit){
					System.out.println("Would you like to play again? (\"y\" for yes)");
					String a = sc.nextLine();
					if(a.equals("y") || a.equals("Y")){
						//call the main method of the "next" game instance
						String[] args={};
						GameShow.main(args);
						System.out.println("");
					}
				}
				
				else{
					sc.close(); //close the scanner
				}
				

				//close the current instance of the game

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
		
		public Read(int ansIndex, String userAns){
			trueAns = answers.get(ansIndex);
			if(!isNumeric(trueAns)){ //toLowerCase seems to mess up numeric strings
		    	String temp = trueAns.toLowerCase(); // case support
		    	trueAnsLOW = temp;
		    }
			else{
				//if it is numeric then it is the same, no lower case format
				trueAnsLOW = trueAns;
			}
			answers.remove(ansIndex);
			this.userAns = userAns;
		}
		
		public void run() {
			lock.lock();
			//read in answer
			try{
				if(timeFlag == 0){
					if(userAns.compareTo(trueAnsLOW)==0){
						points++;
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


