package uk.me.rkd.jsipp.runtime;

public class RateIncreaseThread extends Thread {

	private double increase;
	private long pause;
	private double max;

	public RateIncreaseThread(double increase, long pause, double max) {
		this.increase = increase;
		this.pause = pause;
		this.max = max;
	}

	@Override
	public void run() {
		double currentRate = CallOpeningTask.getInstance().getRate();
		while (currentRate < this.max) {
			System.out.println("Increasing rate...");
			CallOpeningTask.getInstance().setRate(currentRate + this.increase);
			try {
				sleep(this.pause);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			currentRate = CallOpeningTask.getInstance().getRate();
		}
		System.out.println("Quitting rate increase thread...");
	}
}
