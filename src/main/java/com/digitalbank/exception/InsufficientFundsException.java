package com.digitalbank.exception;
public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException() { super("Saldo insuficiente para realizar a operação."); }
}
