package xserver;
import java.util.LinkedList;
import java.util.Queue;
public class JobQueue {
	Queue<String> queue;
	public JobQueue() {
		queue = new LinkedList<>();
	}
	
	public void put(String job) {
		queue.add(job);
	}
	
	public String poll() {
		return queue.poll();
	}
}
