package io.github.richardstartin.multimatcher.benchmarks;

public class DomainObject {
    private final String currency;
    private final long ipAddress;
    private final long timestamp;
    private final long amount;
    private final double rating;
    private final int id;

    public DomainObject(String currency, long ipAddress, long timestamp, long amount, double rating, int id) {
        this.currency = currency;
        this.ipAddress = ipAddress;
        this.timestamp = timestamp;
        this.amount = amount;
        this.rating = rating;
        this.id = id;
    }

    public String getCurrency() {
        return currency;
    }

    public long getIpAddress() {
        return ipAddress;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getAmount() {
        return amount;
    }

    public double getRating() {
        return rating;
    }

    public int getId() {
        return id;
    }
}
