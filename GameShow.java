/* Name: Uyen-Dung Vo
 * UIN: 424008044
 * Acknowledgments: N/A
 */

import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.Timer;
import java.util.ArrayList;
import java.util.Scanner;

public class GameShow {
	private static int points = 0;
	private static Lock lock= new ReentrantLock();
	private static int timeFlag = 0;
	private static boolean answered = false; 
	private static Condition answer = lock.newCondition();

	public static void main(String[] args) {
		    // Create tasks
			System.out.println("Welcome to the game. Case matters. Enter \"X\" to quit.");
		    Runnable printQuestion = new PrintQ();
		    
		    // Create threads
		    Thread thread1 = new Thread(printQuestion);

		    // Start threads
		    thread1.start();
	  }
	
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
					Thread thread3 = new Thread(timer);
				    thread3.start();
				    
					userAns = sc.nextLine();
				    
					if(userAns.equals("X")){
						thread3.interrupt();
						break;						
					}
				    
				    Runnable reader = new Read(i, userAns);
				    Thread thread2 = new Thread(reader);
				    thread2.start();
				    answer.await();
				    if(timeFlag == 1){
						continue;
					}
				    thread3.interrupt(); //I do this so the timer does not overlap into the next question
				    					//This is on purpose and my way to get around using Thread.sleep instead of Timer
				    
				}
				System.out.println("Game end! Total points: " + points);
				sc.close();
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


