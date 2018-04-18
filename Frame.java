
public class Frame {
	
	public int process_id;		// process id of the process which is using this frame
	public int page_num;		// page number of current reference of the process using this frame
	public int status; 			// 0 for unreferenced, 1 for referenced
	public int initial_time;	// time when this frame started to be referenced by the current process
	public int last_access;		// time when this frame was accessed by the current process

	//Initialization
	
	public Frame(int process_id, int page_num, int time){
		
		this.page_num = page_num;
		this.process_id = process_id;
		this.initial_time = time;
		this.status = 0;
		this.last_access = 0;
		
	}
}
