package ibsp.metaserver.utils;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AsyncCallResult<T> implements Future<T> {

	private final Object sync;

	private boolean isDone;

	private boolean isCancelled;
	private Throwable failure;

	protected T result;

	protected Object[] args;

	public Object[] getArgs() {
		return this.args;
	}

	public AsyncCallResult() {
		this(new Object());
	}

	public AsyncCallResult(Object... args) {
		this();
		this.args = args;
	}

	public AsyncCallResult(Object sync) {
		this.sync = sync;
	}

	/**
	 * Get current result value without any blocking.
	 * 
	 * @return current result value without any blocking.
	 */
	public T getResult() {
		synchronized (this.sync) {
			return this.result;
		}
	}

	/**
	 * Set the result value and notify about operation completion.
	 * 
	 * @param result
	 *            the result value
	 */
	public void setResult(T result) {
		synchronized (this.sync) {
			this.result = result;
			this.notifyHaveResult();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean cancel(boolean mayInterruptIfRunning) {
		synchronized (this.sync) {
			this.isCancelled = true;
			this.notifyHaveResult();
			return true;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isCancelled() {
		synchronized (this.sync) {
			return this.isCancelled;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isDone() {
		synchronized (this.sync) {
			return this.isDone;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public T get() throws InterruptedException, ExecutionException {
		synchronized (this.sync) {
			for (;;) {
				if (this.isDone) {
					if (this.isCancelled) {
						throw new CancellationException();
					} else if (this.failure != null) {
						throw new ExecutionException(this.failure);
					} else if (this.result != null) {
						return this.result;
					}
				}

				this.sync.wait();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		long startTime = System.currentTimeMillis();
		long timeoutMillis = TimeUnit.MILLISECONDS.convert(timeout, unit);
		synchronized (this.sync) {
			for (;;) {
				if (this.isDone) {
					if (this.isCancelled) {
						throw new CancellationException();
					} else if (this.failure != null) {
						throw new ExecutionException(this.failure);
					} else if (this.result != null) {
						return this.result;
					}
				} else if (System.currentTimeMillis() - startTime > timeoutMillis) {
					throw new TimeoutException();
				}

				this.sync.wait(timeoutMillis);
			}
		}
	}

	/**
	 * Notify about the failure, occured during asynchronous operation
	 * execution.
	 * 
	 * @param failure
	 */
	public void failure(Throwable failure) {
		synchronized (this.sync) {
			this.failure = failure;
			this.notifyHaveResult();
		}
	}

	/**
	 * Notify blocked listeners threads about operation completion.
	 */
	protected void notifyHaveResult() {
		this.isDone = true;
		this.sync.notifyAll();
	}
	
}
