import java.util.*;
import java.io.*;

public class Driver {
	
	private static Scanner rand_sc;						// random number scanner
	private static int M,P,S,J,N;						// input integer parameters
	private static String R;							// replacement algorithm
	private static int debug_status;					// 0 for standard output, 1 for detailed output
	
	private static final int quantum = 3;				// quantum is set at 3			
	private static ArrayList<Process> process_list;		// list of processes
	private static ArrayList<Frame> frame_table;		// frame table
	
	// Load the input parameters, initialize process list and the frame table
	
	public static void load(String args[]) throws FileNotFoundException{
		
		rand_sc = new Scanner(new File("random-numbers"));
		
		if (args.length == 7){
			M = Integer.parseInt(args[0]);
			P = Integer.parseInt(args[1]);
			S = Integer.parseInt(args[2]);
			J = Integer.parseInt(args[3]);
			N = Integer.parseInt(args[4]);
			R = args[5];
			debug_status = Integer.parseInt(args[6]);
		}else{
			System.out.println("invalid input, please use the input structure in readme.txt");
			System.exit(1);
		}
		
		System.out.println("The machine size is " + M);
		System.out.println("The page size is " + P);
		System.out.println("The process size is " + S);
		System.out.println("The job-mix number is " + J);
		System.out.println("The number of references per process is " + N);
		System.out.println("The replacement algorithm is " + R);
		System.out.println("The debug level is " + debug_status);
		System.out.println();
		
		process_list = new ArrayList<Process>();
		
		switch (J){
		case 1:
			Process p = new Process(1.0, 0.0, 0.0, P, S, N, 0);
			process_list.add(p);
			break;
		case 2:
			for (int i = 0; i < 4; i++){
				Process p1 = new Process (1.0, 0.0, 0.0, P, S, N, i);
				process_list.add(p1);
			}
			break;
		case 3:
			for (int i = 0; i < 4; i++){
				Process p2 = new Process (0.0, 0.0 ,0.0, P, S , N, i);
				process_list.add(p2);
			}
			break;
		case 4:
			Process p3 = new Process (0.75, 0.25, 0, P, S, N, 0);
			Process p4 = new Process (0.75, 0.0, 0.25, P, S, N, 1);
			Process p5 = new Process (0.75, 0.125, 0.125, P, S, N, 2);
			Process p6 = new Process (0.5, 0.125, 0.125, P, S, N, 3);
			process_list.add(p3);
			process_list.add(p4);
			process_list.add(p5);
			process_list.add(p6);
			break;
		default:
			System.out.println("invalid input for J");
			System.exit(1);
			break;
		}
		
		frame_table = new ArrayList<Frame>();
		int total_frame_num = M / P;
		for (int i = 0; i < total_frame_num; i++){
			Frame f = new Frame(-1,-1,-1);
			frame_table.add(f);
		}
		
	}
	
	// Check whether the current reference of the process is in the frame table, return the index of the hitfrmae
	// If there is no hit, return -1
	
	public static int checkHit(Process p,ArrayList<Frame> frame_table){
		for (int i = 0; i < frame_table.size(); i++){
			Frame cur_frame = frame_table.get(i);
			int cur_page = p.cur_ref / P;
			if (p.process_id == cur_frame.process_id && cur_page == cur_frame.page_num && cur_frame.status == 1){
				return i;
			}
		}
		return -1;
	}
	
	// Check whether the frmae table is full, return the index of the highest numbered free frame
	// If the frame table is full, return -1
	
	public static int checkFull(ArrayList<Frame> frame_table){
		for (int i = frame_table.size() - 1; i > -1; i--){
			Frame cur_frame = frame_table.get(i);
			if (cur_frame.status == 0){
				return i;
			}
		}
		return -1;
	}
	
	// Find the replaced frame using LRU algorithm
	
	public static int replace_lru(ArrayList<Frame> frame_table){
		// here smallest has to be max_value or there may be problems in case of input 14,16
		int smallest = Integer.MAX_VALUE;
		int index = -1;
		for (int i = 0; i < frame_table.size(); i++){
			Frame cur_frame = frame_table.get(i);
			if (cur_frame.last_access < smallest){
				smallest = cur_frame.last_access;
				index = i;
			}
		}
		return index;
	}
	
	// Find the replaced frame using FIFO algorithm
	
	public static int replace_fifo(ArrayList<Frame> frame_table){
		// here smallest has to be max_value or there may be problems in case of input 14,16
		int smallest = Integer.MAX_VALUE;
		int index = -1;
		for (int i = 0; i < frame_table.size(); i++){
			Frame cur_frame = frame_table.get(i);
			if (cur_frame.initial_time < smallest){
				smallest = cur_frame.initial_time;
				index = i;
			}
		}
		return index;
	}
	
	//Find the replaced frame using random number algorithm
	
	public static int replace_random(ArrayList<Frame> frame_table){
		int rand_num = rand_sc.nextInt();
		int index = rand_num % (frame_table.size());
		return index;
	}
	
	public static void run(){
		
		int time = 1;									// virtual clock starting from 1
		int finished = 0;								// number of finished processes
		
		while (finished < process_list.size()){
			
			for (int i = 0; i < process_list.size(); i++){
				
				for (int j = 0; j < quantum; j++){
					
					Process cur_process = process_list.get(i);
					// Check whether this process has finished
					if (cur_process.status == 1){
						break;
					}
					// Check whether this process can be finished in this clock period
					if (cur_process.ref_num == N){
						finished ++;
						cur_process.status = 1;
						break;
					}
					
					int cur_page = cur_process.cur_ref / P;
					int check = checkHit(cur_process, frame_table);
					
					// If the current reference of the process is in the frame table
					
					if (check!= -1){
						Frame temp = frame_table.get(check);
						// Update the last access time
						temp.last_access = time;
						int temp_id = cur_process.process_id + 1;
						if (debug_status == 1){
							System.out.println(temp_id  + " references word " + cur_process.cur_ref +
									" (Page " + cur_page + ") at time " + time + " :Hit in frame " + check);
						}
					}
					
					// If the current reference of the process is not in the frame table
					
					else{
						int check1 = checkFull(frame_table);
						
						// If the frmae table is not full, find the highest numbered free frame
						
						if (check1 != -1){
							Frame free = frame_table.get(check1);
							free.process_id = cur_process.process_id;
							free.page_num = cur_page;
							free.status = 1;
							free.initial_time = time;
							free.last_access = time;
							int temp_id = cur_process.process_id + 1;
							if (debug_status == 1){
								System.out.println(temp_id  + " references word " + cur_process.cur_ref +
										" (Page " + cur_page + ") at time " + time + " :Fault, using free frame " + check1);
							}
							cur_process.fault_num ++;
						}
						
						// If the frame table is full, use replacement algorithm to replace one of the frames
						
						else{
							int index = -1;
							
							if (R.equals("lru")){
								index = replace_lru(frame_table);
							}else if (R.equals("fifo")){
								index = replace_fifo(frame_table);
							}else if (R.equals("random")){
								index = replace_random(frame_table);
							}else{
								System.out.println("invalid replacement algorithm");
								System.exit(1);
							}
							
							// Update page fault information
							
							Frame replaced_frame = frame_table.get(index);
							int replaced_page = replaced_frame.page_num;
							int id = replaced_frame.process_id;
							Process replaced_process = process_list.get(id);
							replaced_process.eviction_num ++;
							replaced_process.run_total += time - replaced_frame.initial_time;
							
							// Replace the selected frame
							
							replaced_frame.initial_time = time;
							replaced_frame.last_access = time;
							replaced_frame.process_id = cur_process.process_id;
							replaced_frame.page_num = cur_page;
							replaced_frame.status = 1;
							int temp_id = cur_process.process_id + 1;
							if (debug_status == 1){
								System.out.println(temp_id  + " references word " + cur_process.cur_ref +
										" (Page " + cur_page + ") at time " + time + " :Fault, evicting page " + 
										replaced_page + " of " + (replaced_process.process_id + 1) + " from Frame " + index);
							}
							cur_process.fault_num ++;
						}
					}
					cur_process.nextReference(rand_sc);
					cur_process.ref_num ++;
					time ++;
				}
			}
		}
		
		/*for (int i = 0; i < frame_table.size(); i++){
			Frame f = frame_table.get(i);
			int id = f.process_id;
			Process p = process_list.get(id);
			p.run_total += time - f.initial_time;
		}
		*/
	}
	
	// Output
	
	public static void output(){
		
		System.out.println();
		int total_faults = 0;
		int total_residency = 0;
		int total_evictions = 0;
		
		for (int i = 0; i < process_list.size(); i++){
			Process p = process_list.get(i);
			total_faults += p.fault_num;
			total_residency += p.run_total;
			total_evictions += p.eviction_num;
			System.out.print("Process " + (p.process_id + 1) + " has " + p.fault_num + " faults " );
			if (p.run_total == 0){
				System.out.println();
				System.out.println("With no evictions, the average residence is undefined");
			}else{
				double average_residency = (double) p.run_total / p.eviction_num;
				System.out.print("and " + average_residency + " average residency" + "\n");
			}
			
		}
		
		double total_average_residency = (double) total_residency / total_evictions;
		
		System.out.println();
		if (total_residency != 0){
			System.out.println("The total number of faults is " + total_faults + 
					" ,the overall average residency is " + total_average_residency);
		}else{
			System.out.println("The total number of faults is " + total_faults + 
					" ,the overall average residency is undefined");
		}
	}
	
	public static void main (String args[]) throws FileNotFoundException{
		load(args);
		run();
		output();
	}
	
}
