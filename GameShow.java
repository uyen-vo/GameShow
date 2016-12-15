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

//static -> inner classes don't have access to private of outer class

public class GameShow {
	private static int points;
	private static Lock lock;
	private static int timeFlag;
	private static boolean answered; 
	private static Condition answer;
	
	public GameShow(){
		//constructor to initialize
		points = 0;
		lock = new ReentrantLock();
		timeFlag = 0;
		answered = false;
		answer = lock.newCondition();
	}

	public static void main(String[] args) {
		// Create tasks
		GameShow g = new GameShow();
		System.out.println("Welcome to the game. Case matters. Enter \"X\" to quit.");
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
	public static class PrintQ implements Runnable{
			private ArrayList<String> questions = new ArrayList();
			private Scanner sc = new Scanner(System.in);
			private String userAns;

			public PrintQ(){
				questions.add("Who was the first president of the U.S.?");
				questions.add("What is Obama's first name?");
				questions.add("What is Professor Dutta's first name?");
				questions.add("What is 2+2? (Number)");
			}
			
			
			@Override
			public void run() {
				lock.lock();
				try{
				for(int i = 0; i<questions.size(); i++){
					System.out.println("Current points: " + points);
					System.out.println(questions.get(i));
					timeFlag = 0;
					answered = false;
					Runnable timer = new TimeTen();
					Thread threadT = new Thread(timer);
				    threadT.start();
				    
					userAns = sc.nextLine();
				    
					if(userAns.equals("X")){
						threadT.interrupt();
						break;						
					}
				    
				    Runnable reader = new Read(i, userAns);
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
				System.out.println("Would you like to play again? (y/n)");
				String a = sc.nextLine();
				if(a.equals("y") || a.equals("Y")){
					//call the main method
					String[] args={};
					GameShow.main(args);
				}

				sc.close(); //close the current instance of the game

				} catch (Exception e){
					e.printStackTrace();
				}
				finally{
					lock.unlock();
				}
			}
			
	}
	  
	public static class Read implements Runnable{

		ArrayList<String> answers = new ArrayList();
		String trueAns;
		String userAns;
		
		public Read(int ansIndex, String userAns){
			answers.add("George Washington");
			answers.add("Barack");
			answers.add("Anandi");
			answers.add("4");
			trueAns = answers.get(ansIndex);
			this.userAns = userAns;
		}
		
		public void run() {
			lock.lock();
			//read in answer
			try{
				if(timeFlag == 0){
					if(userAns.compareTo(trueAns)==0){
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


