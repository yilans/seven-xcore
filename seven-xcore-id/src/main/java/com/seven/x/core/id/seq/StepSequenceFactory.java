package com.seven.x.core.id.seq;

/**
 * 支持逐一递增的序号生成工厂
 * @author yan.jsh
 * 2011-7-10
 *
 */
public class StepSequenceFactory extends TableSequenceFactory {
	private int step;
	private long id;
	private int nextStep;
	private long timestamp;

	public StepSequenceFactory() {
		setStep(10);
	}

	public void setStep(int step) {
		this.step = step;
		this.nextStep = (this.step + 1);
	}

	protected long[] internalGenerate(int step) {
		if (step > 1) {
			throw new IllegalArgumentException("step sequence cannot generate more than 1 id once.");
		}
		synchronized (this) {
			if ((this.nextStep > this.step) || ((this.isDateCutoff) && (this.table.getTimeSerivce().isCutoff(5, this.timestamp)))) {
				this.nextStep = 1;
				
				long[] result = super.internalGenerate(this.step);
				
				this.id = result[0];
				this.timestamp = result[1];
			}

			return new long[] { this.id + this.nextStep++, this.timestamp };
		}
	}
}
