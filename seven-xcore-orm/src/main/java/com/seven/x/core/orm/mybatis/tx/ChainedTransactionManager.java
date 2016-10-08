package com.seven.x.core.orm.mybatis.tx;

import java.util.ArrayList;

import java.util.List;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

import org.springframework.transaction.PlatformTransactionManager;

import org.springframework.transaction.TransactionDefinition;

import org.springframework.transaction.TransactionException;

import org.springframework.transaction.TransactionStatus;

import org.springframework.transaction.TransactionSystemException;

import org.springframework.transaction.support.TransactionSynchronizationManager;

public class ChainedTransactionManager implements PlatformTransactionManager {
	
	protected Logger logger = LoggerFactory.getLogger(getClass());

	private List<PlatformTransactionManager> transactionManagers = new ArrayList<PlatformTransactionManager>();

	public void setTransactionManagers(List<PlatformTransactionManager> transactionManagers) {
		this.transactionManagers = transactionManagers;
	}

	public TransactionStatus getTransaction(TransactionDefinition transactionDefinition) throws TransactionException {
		MultiTransactionStatus status = new MultiTransactionStatus((PlatformTransactionManager) this.transactionManagers.get(0));

		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.initSynchronization();
			status.setNewSynchonization();
		}

		for (PlatformTransactionManager manager : this.transactionManagers) {
			status.getTransactionStatuses().put(manager, manager.getTransaction(transactionDefinition));
		}

		return status;
	}

	public void commit(TransactionStatus status) throws TransactionException {
		int i = 1;
		Exception exception = null;
		Object _manager = null;

		for (int j = this.transactionManagers.size() - 1; j >= 0; j--) {
			PlatformTransactionManager manager = (PlatformTransactionManager) this.transactionManagers.get(j);

			if (i != 0)
				try {
					manager.commit((TransactionStatus) ((MultiTransactionStatus) status).getTransactionStatuses().get(manager));
				} catch (Exception e) {
					i = 0;
					exception = e;
					_manager = manager;
				}
			else {
				try {
					manager.rollback((TransactionStatus) ((MultiTransactionStatus) status).getTransactionStatuses().get(manager));
				} catch (Exception e) {
					this.logger.warn("Rollback exception (after commit) (" + manager + ") " + e.getMessage(), e);
				}
			}
		}

		if (((MultiTransactionStatus) status).isNewSynchonization()) {
			TransactionSynchronizationManager.clear();
		}

		if (exception != null)
			throw new TransactionSystemException("Commit exception (" + _manager + ") " + exception.getMessage(), exception);
	}

	public void rollback(TransactionStatus status) throws TransactionException {
		Exception exception = null;
		Object _manager = null;

		for (int i = this.transactionManagers.size() - 1; i >= 0; i--) {
			PlatformTransactionManager manager = (PlatformTransactionManager) this.transactionManagers.get(i);
			
			try {
				manager.rollback((TransactionStatus) ((MultiTransactionStatus) status).getTransactionStatuses().get(manager));
			} catch (Exception e) {
				if (exception == null) {
					exception = e;
					_manager = manager;
				} else {
					this.logger.warn("Rollback exception (" + manager + ") " + e.getMessage(), e);
				}
			}
		}

		if (((MultiTransactionStatus) status).isNewSynchonization()) {
			TransactionSynchronizationManager.clear();
		}

		if (exception != null)
			throw new TransactionSystemException("Rollback exception (" + _manager + ") " + exception.getMessage(), exception);
	}

}
