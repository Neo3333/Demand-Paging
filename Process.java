import java.util.*;

public class Process {
	
	public double A;
	public double B;
	public double C;
	public int P; 					// page size
	public int S;					// process size
	public int N;					// number of references
	public int cur_ref;     		// current reference
	public int ref_num;				// number of references taken
	public int fault_num;			// number of faults
	public int eviction_num;		// number of evictions
	public int run_total;			// total resisdency time for this process
	public int process_id;			// the index of this process in process list
	public int status;				// 0 for not finished, 1 for finished
	
	// Initialization
	
	public Process(double A,double B,double C,int P, int S, int N, int process_id){
		
		this.A = A;
		this.B = B;
		this.C = C;
		this.P = P;
		this.S = S;
		this.N = N;
		this.status = 0;
		this.process_id = process_id;
		
		this.ref_num = 0;
		this.fault_num = 0;
		this.eviction_num = 0;
		this.run_total = 0;
		// initial reference number
		this.cur_ref = (111 * (process_id + 1)) % S; 
		
	}
	
	// Generate next reference number based on A,B,C of this process
	
	public void nextReference(Scanner rand_sc){
		
		int next_ref = 0;
		
		int rand_num = rand_sc.nextInt();
		
		double p = rand_num / (Integer.MAX_VALUE + 1d);
		
		if (p < this.A){
			next_ref = (cur_ref + 1) % this.S;
		}else if (p < this.A + this.B){
			next_ref = (cur_ref + S - 5) % this.S;
		}else if (p < this.A + this.B + this.C){
			next_ref = (cur_ref + 4) % this.S;
		}else{
			int temp = rand_sc.nextInt();
			next_ref = temp % this.S;
		}
		
		this.cur_ref = next_ref;

	}
}
