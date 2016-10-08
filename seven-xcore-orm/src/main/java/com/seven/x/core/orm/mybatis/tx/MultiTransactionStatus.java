package com.seven.x.core.orm.mybatis.tx;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;

public class MultiTransactionStatus implements TransactionStatus {
	
	private PlatformTransactionManager platformTransactionManager;
	
	private Map<PlatformTransactionManager, TransactionStatus> transactionStatuses = Collections.synchronizedMap(new HashMap<PlatformTransactionManager, TransactionStatus>());
	
	private boolean newSynchonization;

	public Map<PlatformTransactionManager, TransactionStatus> getTransactionStatuses() {
		return this.transactionStatuses;
	}

	public MultiTransactionStatus(PlatformTransactionManager manager) {
		this.platformTransactionManager = manager;
	}

	private TransactionStatus getTransactionStatus() {
		return (TransactionStatus) this.transactionStatuses.get(this.platformTransactionManager);
	}

	public void setNewSynchonization() {
		this.newSynchonization = true;
	}

	public boolean isNewSynchonization() {
		return this.newSynchonization;
	}

	public boolean isNewTransaction() {
		return getTransactionStatus().isNewTransaction();
	}

	public boolean hasSavepoint() {
		return getTransactionStatus().hasSavepoint();
	}

	public void setRollbackOnly() {
		for (TransactionStatus status : this.transactionStatuses.values())
			status.setRollbackOnly();
	}

	public boolean isRollbackOnly() {
		return getTransactionStatus().isRollbackOnly();
	}

	public boolean isCompleted() {
		return getTransactionStatus().isCompleted();
	}

	public Object createSavepoint() throws TransactionException {
		return getTransactionStatus().createSavepoint();
	}

	public void rollbackToSavepoint(Object obj) throws TransactionException {
		for (TransactionStatus status : this.transactionStatuses.values())
			status.rollbackToSavepoint(obj);
	}

	public void releaseSavepoint(Object obj) throws TransactionException {
		for (TransactionStatus status : this.transactionStatuses.values())
			status.releaseSavepoint(obj);
	}

	public void flush() {
	}
}
