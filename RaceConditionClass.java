package RaceCondition;

import java.util.Arrays;
import java.util.concurrent.*;

public class RaceConditionClass {
	//this is a class meant to observe and correct a race condition vulnerability
	
	public static void main(String[] args) {
		//run one of these two methods in order to see either a race condition or the semaphores in action 
		
		createRaceCondition(); 		//use this to see a race condition occur
		//eliminateRaceCondition();	//use this in order to run the program with semaphores
	}
	
	private static void createRaceCondition(){
		/*
		 * This method uses the ProducerThread and ConsumerThread class in order to simulate a race condition.
		 * Both classes are initialized to use the same array, thus they will be modifying the same array. 
		 * Since both classes extend threads, they will start and run concurrently.
		 */
		int size = 15;
		int[] array = new int[size];
		
		ProducerThread producer = new ProducerThread(array);
		ConsumerThread consumer = new ConsumerThread(array);
		
		producer.start();
		consumer.start();
	}
	
	private static void eliminateRaceCondition(){
		/*
		 * This methods essentially runs the same program as the createRaceCondition method,
		 * but uses semaphores in order to prevent the race condition.
		 * This method utilizes the BetterProducerThread and BetterConsumerThread classes,
		 * and they are initialized with the same array and semaphore.
		 * Both classes extends threads and will start and run concurrently. 
		 */
		int size = 15;
		int[] array = new int[size];
		Semaphore sem = new Semaphore(0);
		
		BetterProducerThread producer = new BetterProducerThread(array, sem);
		BetterConsumerThread consumer = new BetterConsumerThread(array, sem);
		
		producer.start();
		consumer.start();
	}
	
	public static class ProducerThread extends Thread{
		/*
		 * ProducerThread class fills the buffer with a random number of
		 * ones and sleeps for a random amount of time.
		 * Vulnerable to race condition  
		 */
		int[] buffer; 
		int n;
		
		public ProducerThread(int[] array) {
			buffer = array;
			n = array.length;
		}
		
		public void run() {
			int next_in = 0;
			while(true) {
				int k1 =((int)(Math.random()*10))+1;;
				//System.out.println("k1: " + k1);
				for (int i = 0; i < k1; i++)
					buffer[(next_in + i) % n] += 1; 
				System.out.println(Arrays.toString(buffer));
				next_in = (next_in + k1) % n;
				int t1 = ((int)(Math.random()*900))+100;
				//System.out.println("t1: " + t1);
				try {
					Thread.sleep(t1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static class ConsumerThread extends Thread{
		/*
		 * The ConsumerThread class consumes a random number of 
		 * ones in the buffer and sleeps for a random number of times.
		 * Vulnerable to race condition
		 */
		int[] buffer; 
		int n;
		
		public ConsumerThread(int[] array) {
			buffer = array;
			n = array.length;
		}
		
		public void run() {
			int next_out = 0;
			while(true) {
				int data;
				int t2 = ((int)(Math.random()*900))+100;
				//System.out.println("t2: " + t2);
				try {
					Thread.sleep(t2);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				int k2 = ((int)(Math.random()*10))+1;
				//System.out.println("k2: " + k2);
				for (int i = 0; i < k2; i++) {
					data = buffer[((next_out + i) % n)]; 
					buffer[((next_out + i) % n)] -= 1;
					System.out.println(Arrays.toString(buffer));
					if(data != 1) {
						System.out.println("Race Condition Detected");
						System.exit(0);
					}
				}
				next_out = ((next_out + k2) % n);
			}
		}
	}
	
	public static class BetterProducerThread extends Thread{
		/*
		 * The BetterProducerThread class uses semaphores when 
		 * filling the buffer in order to eliminate the 
		 * race condition problem. 
		 */
		int[] buffer;
		Semaphore sem;
		int n;
		
		public BetterProducerThread(int[] array, Semaphore s) {
			buffer = array;
			sem = s;
			n = array.length;
		}
		
		public void run() {
			int next_in = 0;
			while(true) {
				int k1 =((int)(Math.random()*10))+1;;
				//System.out.println("k1: " + k1);
				for (int i = 0; i < k1; i++) {
					sem.release();
					//System.out.println(sem.availablePermits());
					while(sem.availablePermits() == n) {	//force producer to stop producing
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					buffer[(next_in + i) % n] += 1; 
				}
				System.out.println(Arrays.toString(buffer));
				next_in = (next_in + k1) % n;
				int t1 = ((int)(Math.random()*900))+100;
				//System.out.println("t1: " + t1);
				try {
					Thread.sleep(t1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static class BetterConsumerThread extends Thread{
		/*
		 * The BetterConsumerThread class uses semaphores when 
		 * emptying the buffer in order to eliminate the 
		 * race condition problem. 
		 */
		int[] buffer;
		Semaphore sem;
		int n;
			
		public BetterConsumerThread(int[] array, Semaphore s) {
			buffer = array;
			sem = s;
			n = array.length;
		}
		
		public void run() {
			int next_out = 0;
			while(true) {
				int data;
				int t2 = ((int)(Math.random()*900))+100;
				//System.out.println("t2: " + t2);
				try {
					Thread.sleep(t2);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				int k2 = ((int)(Math.random()*10))+1;
				//System.out.println("k2: " + k2);
				for (int i = 0; i < k2; i++) {
					while(sem.availablePermits() == 0) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					try {
						sem.acquire();
					} catch (InterruptedException e) {	
						e.printStackTrace();
					}
					//System.out.println(sem.availablePermits());
					data = buffer[((next_out + i) % n)]; 
					buffer[((next_out + i) % n)] -= 1;
					if(data != 1) {
						System.out.println("Race Condition Detected");
						System.exit(0);
					}
				}
				System.out.println(Arrays.toString(buffer));
				next_out = ((next_out + k2) % n);
			}
		}
	}
}
