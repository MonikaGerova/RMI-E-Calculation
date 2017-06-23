import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Calendar;

class Task implements Runnable {
	
	private static final int PREC = 30;
	
	private int taskNumber;
	private String name;
	PrintWriter writer;	
	boolean quiet;

	Task(String name, int taskNumber, PrintWriter writer, boolean quiet) {
		this.name = name; 
		this.taskNumber = taskNumber; 
		this.writer = writer;
		this.quiet = quiet;
	}
	
	//Factorial function
	static BigDecimal memoizedFact(int n) {
		BigDecimal r = BigDecimal.ONE;
		for(int i = 1; i <= n; ++i) {
			r = r.multiply(new BigDecimal(i));
		}
		return r;
	}
	
	@Override
	public void run() {
		
		long start = Calendar.getInstance().getTimeInMillis();
		//check quiet functionality
		if(!quiet) {
			writer.println(name + " started.");
		}
		
		//why i is equal to taskNumber and why iterate with threadsNum
		for(int i = this.taskNumber; i <= Program.n ; i += Program.threadsNum) {
			BigDecimal d = new BigDecimal(3*i*3*i+1);//numerator
			d = d.divide(memoizedFact(3*i), PREC, BigDecimal.ROUND_HALF_UP);//denominator
			//critical section 
			synchronized (Program.res) {
				Program.res = Program.res.add(d); //sum
			}
		}
		
		
		//time calculation
		long end = Calendar.getInstance().getTimeInMillis();
		if(!quiet) {
		
			writer.println(name + " stopped.");
			writer.println(name + " execution time was (millis): " + (end - start));
		}
	}
	
}


class Program {
	
	public static int n = 3500; 	//number of members of the line
	public static int threadsNum = 10;	//number of threads
	
	static BigDecimal res = BigDecimal.ZERO;	//global variable where sum all members
	
	public static void main(String[] args) throws Exception {
		
		long start = Calendar.getInstance().getTimeInMillis();
		boolean quiet = false;
		String filePath = null;
		
		//manage command parameters
		for (int i=0; i<args.length; i++) {
            if (args[i].equals("-p")) { n = Integer.parseInt(args[i+1]); }
           
            if (args[i].equals("-t")) { threadsNum = Integer.parseInt(args[i+1]); }
           
            if (args[i].equals("-q")) { quiet = true; }
            
            if (args[i].equals("-o")) { filePath = args[i+1]; }
        }
		
		//file 
		if(filePath == null)
			filePath = "result.txt";
		new File(filePath).createNewFile();
		
		/*
		Threads creation and start them
			1)get the file
			2)create pull of threads
			3)every thread create new Task
				3.1)calculate  
			4)join all threads
		*/
		try(PrintWriter writer = new PrintWriter(new FileOutputStream(filePath), true)) {
			Thread[] threads = new Thread[threadsNum];
			for (int i = 0; i < threadsNum; ++i) {
				threads[i] = new Thread(new Task("Thread" + (i + 1), i, writer, quiet));
				threads[i].start();
			}
			for (int i = 0; i < threadsNum; i++) {
				try {
					threads[i].join();
				} catch (InterruptedException err) {
					err.printStackTrace();
				}
			}
			
			//timing
			long end = Calendar.getInstance().getTimeInMillis();
			writer.println("Total execution time for current run (millis): " + (end - start));
			writer.println("Result : " + res.toString());
		} 
	}

}