package com.moneytransfer.dao.impl.fast;

import com.moneytransfer.dao.AccountDAO;
import com.moneytransfer.exception.CustomException;
import com.moneytransfer.model.Account;
import com.moneytransfer.model.MoneyUtil;
import com.moneytransfer.model.User;
import com.moneytransfer.model.UserTransaction;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FastAccountDAOImpl implements AccountDAO {

	private Map<Long, Account> accountRepository = new ConcurrentHashMap<>();
	private Map<UserCurrencyPair, Long> userIndex = new ConcurrentHashMap<>();
	private long index = 0L;

	private class UserCurrencyPair {
		private String user;
		private String currency;

		public UserCurrencyPair(Account account) {
			this.user = account.getUserName();
			this.currency = account.getCurrencyCode();
		}

		public UserCurrencyPair(String user, String currency) {
			this.user = user;
			this.currency = currency;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			UserCurrencyPair that = (UserCurrencyPair) o;
			return Objects.equals(user, that.user) &&
					Objects.equals(currency, that.currency);
		}

		@Override
		public int hashCode() {
			return Objects.hash(user, currency);
		}
	}

	@Override
	public Set<Account> getAllAccounts() throws CustomException {
		return new HashSet<>(accountRepository.values());
	}

	@Override
	public Account getAccountById(long accountId) throws CustomException {
		return accountRepository.get(accountId);
	}

	@Override
	public Account getAccountByUser(String user, String currency) throws CustomException {
		Long index = userIndex.get(new UserCurrencyPair(user, currency));
		accountRepository.get(index);
		return accountRepository.get(index);
	}

	@Override
	public long createAccount(Account account) throws CustomException {
		if (userIndex.containsKey(new UserCurrencyPair(account))) {
			return 0;
		}
		index++;
		accountRepository.put(index, new Account(index, account.getUserName(),
				account.getBalance(), account.getCurrencyCode()));
		userIndex.put(new UserCurrencyPair(account), account.getAccountId());
		return index;
	}

	@Override
	public int deleteAccountById(long accountId) throws CustomException {
		Account prev = accountRepository.remove(accountId);
		if (prev == null) {
			return 0;
		}
		userIndex.remove(new UserCurrencyPair(prev));
		return 1;
	}

	@Override
	public int updateAccountBalance(long accountId, BigDecimal deltaAmount) throws CustomException {
		Account account = accountRepository.get(accountId);
		BigDecimal balance = account.getBalance().add(deltaAmount);
		if (balance.compareTo(MoneyUtil.zeroAmount) < 0) {
			throw new CustomException("Not sufficient Fund for account: " + accountId);
		}
		Account newAccount = new Account(account.getAccountId(), account.getUserName(), balance, account.getCurrencyCode());
		accountRepository.put(accountId, newAccount);
		return 1;
	}

	@Override
	// TODO synchronize carefully
	public synchronized int transferAccountBalance(UserTransaction userTransaction) throws CustomException {
		Long fromId = userTransaction.getFromAccountId();
		Account accountFrom = accountRepository.get(fromId);

		Long toId = userTransaction.getToAccountId();
		Account accountTo = accountRepository.get(toId);

		Account first;
		Account second;
		if (accountFrom.getAccountId() > accountTo.getAccountId()) {
			first = accountFrom;
			second = accountTo;
		} else {
			first = accountTo;
			second = accountFrom;
		}

		// check transaction currency
		if (!accountFrom.getCurrencyCode().equals(userTransaction.getCurrencyCode())) {
			throw new CustomException(
					"Fail to transfer Fund, transaction ccy are different from source/destination");
		}

		// check ccy is the same for both accounts
		if (!accountFrom.getCurrencyCode().equals(accountTo.getCurrencyCode())) {
			throw new CustomException(
					"Fail to transfer Fund, the source and destination account are in different currency");
		}

		updateAccountBalance(fromId, userTransaction.getAmount().negate());
		updateAccountBalance(toId, userTransaction.getAmount());
		return 2;
	}

	public void dropTable() {
		accountRepository = new ConcurrentHashMap<>();
		userIndex = new ConcurrentHashMap<>();
		index = 0L;
	}
}
