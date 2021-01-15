package com.example.Lab10Demo;

public class PaymentException extends RuntimeException {

	public enum PaymentErrors {
		USER_NOT_FOUND,
		BAD_CREDENTIALS,
		USER_HAS_NO_ACCOUNT_FOR_CURRENCY,
		ACCOUNT_HAS_NOT_ENOUGH_AMOUNT_FOR_PAYMENT,
		PAYMENT_COULD_NOT_BE_PROCESSED,
		USER_WITH_SAME_EMAIL_ALREADY_EXISTS,
		USER_COULD_NOT_BE_SAVED,
		USER_COULD_NOT_BE_REMOVED,
		ACCOUNT_COULD_NOT_BE_SAVED,
		ACCOUNT_COULD_NOT_BE_REMOVED
	}

	private PaymentErrors error;

	private PaymentException(PaymentErrors error) {
		this.error = error;
	}

	public PaymentErrors getError() {
		return error;
	}

	@Override
	public String toString() {
		return error.name().toUpperCase();
	}

	public static PaymentException userNotFound() {
		return new PaymentException(PaymentErrors.USER_NOT_FOUND);
	}

	public static PaymentException badCredentials() {
		return new PaymentException(PaymentErrors.BAD_CREDENTIALS);
	}

	public static PaymentException userHasNoAccountForCurrency() {
		return new PaymentException(PaymentErrors.USER_HAS_NO_ACCOUNT_FOR_CURRENCY);
	}

	public static PaymentException accountHasNotEnoughAmountForPayment() {
		return new PaymentException(PaymentErrors.ACCOUNT_HAS_NOT_ENOUGH_AMOUNT_FOR_PAYMENT);
	}

	public static PaymentException paymentCouldNotBeProcessed() {
		return new PaymentException(PaymentErrors.PAYMENT_COULD_NOT_BE_PROCESSED);
	}

	public static PaymentException userWithSameEmailAlreadyExists() {
		return new PaymentException(PaymentErrors.USER_WITH_SAME_EMAIL_ALREADY_EXISTS);
	}

	public static PaymentException userCouldNotBeSaved() {
		return new PaymentException(PaymentErrors.USER_COULD_NOT_BE_SAVED);
	}

	public static PaymentException userCouldNotBeRemoved() {
		return new PaymentException(PaymentErrors.USER_COULD_NOT_BE_REMOVED);
	}

	public static PaymentException accountCouldNotBeSaved() {
		return new PaymentException(PaymentErrors.ACCOUNT_COULD_NOT_BE_SAVED);
	}

	public static PaymentException accountCouldNotBeRemoved() {
		return new PaymentException(PaymentErrors.ACCOUNT_COULD_NOT_BE_REMOVED);
	}
}
