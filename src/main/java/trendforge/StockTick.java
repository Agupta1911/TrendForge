package trendforge;

public class StockTick {
    public final String symbol;
    public final long ts;      // epoch millis
    public final double price;
    public final long volume;

    public StockTick(String symbol, long ts, double price, long volume) {
        this.symbol = symbol;
        this.ts = ts;
        this.price = price;
        this.volume = volume;
    }

    @Override
    public String toString() {
        return String.format("%s,%d,%.4f,%d", symbol, ts, price, volume);
    }
}
