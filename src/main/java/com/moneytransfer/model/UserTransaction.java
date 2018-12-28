package com.moneytransfer.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class UserTransaction {

	@JsonProperty(required = true)
	private String currencyCode;

	@JsonProperty(required = true)
	private BigDecimal amount;

	@JsonProperty(required = true)
	private Long fromAccountId;

	@JsonProperty(required = true)
	private Long toAccountId;

	public UserTransaction() {
	}

	public UserTransaction(String currencyCode, BigDecimal amount, Long fromAccountId, Long toAccountId) {
		this.currencyCode = currencyCode;
		this.amount = amount;
		this.fromAccountId = fromAccountId;
		this.toAccountId = toAccountId;
	}

	public String getCurrencyCode() {
		return currencyCode;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public Long getFromAccountId() {
		return fromAccountId;
	}

	public Long getToAccountId() {
		return toAccountId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		UserTransaction that = (UserTransaction) o;

		if (!amount.equals(that.amount))
			return false;
		if (!toAccountId.equals(that.toAccountId))
			return false;
		if (!fromAccountId.equals(that.fromAccountId))
			return false;
		return currencyCode.equals(that.currencyCode);

	}

	@Override
	public int hashCode() {
		int result = currencyCode.hashCode();
		result = 31 * result + amount.hashCode();
		result = 31 * result + fromAccountId.hashCode();
		result = 31 * result + toAccountId.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "{" + "\"currencyCode\":\"" + currencyCode + '\"' + ", \"amount\":" + amount + ", \"fromAccountId\":"
				+ fromAccountId + ", \"toAccountId\":" + toAccountId + '}';
	}
}
