package com.example.Lab10Demo;

import java.util.UUID;

public class AccountModel {
	
	public static enum Currency {
		RON,
		EUR,
		USD
	}

	private String id;
	private String userId;
	private Currency currency;
	private double amount;
	
	public AccountModel() {
		id = UUID.randomUUID().toString();
	}

	public AccountModel(String id, String userId, Currency currency, double amount) {
		this.id = id;
		this.userId = userId;
		this.currency = currency;
		this.amount = amount;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Currency getCurrency() {
		return currency;
	}

	public void setCurrency(Currency currency) {
		this.currency = currency;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public String getId() {
		return id;
	}
	
}
