package common.instamsg.driver;

public interface WatchDogBeforeRebootHandler<T> {
	public void handle(T event);
}
