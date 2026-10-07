package com.asadaclan.accounting.models;

import java.util.Optional;

// public record Transaction(String items, float transactionAmount, Optional<Float> tipAmount, Optional<String> image) {
public record Transaction(String items, Float transactionAmount, Optional<Float> tipAmount) {

}
