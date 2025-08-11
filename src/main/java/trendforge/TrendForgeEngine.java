package trendforge;

import java.util.*;
import java.util.concurrent.*;
import java.util.Map.Entry;

/**
 * Thread-safe TrendForge engine.
 * Uses ConcurrentHashMap -> ConcurrentSkipListMap for per-symbol ordered ticks.
 */
public class TrendForgeEngine {
    private final ConcurrentHashMap<String, ConcurrentSkipListMap<Long, StockTick>> index = new ConcurrentHashMap<>();

    public void ingestTick(StockTick tick) {
        index.computeIfAbsent(tick.symbol, k -> new ConcurrentSkipListMap<>()).put(tick.ts, tick);
    }

    public void ingestBulk(List<StockTick> ticks, int threads) throws InterruptedException {
        ExecutorService ex = Executors.newFixedThreadPool(Math.max(2, threads));
        for (StockTick t : ticks) {
            ex.submit(() -> ingestTick(t));
        }
        ex.shutdown();
        ex.awaitTermination(5, TimeUnit.MINUTES);
    }

    public void loadCsvMultiThreaded(String path, int threads) throws Exception {
        // lightweight CSV reader with simple sharding by symbol to allow parallel ingestion
        java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(path));
        String hdr = br.readLine(); // skip header if present
        List<StockTick> batch = new ArrayList<>(10000);
        String line;
        while ((line = br.readLine()) != null) {
            String[] a = line.split(",");
            if (a.length < 4) continue;
            String symbol = a[0];
            long ts = Long.parseLong(a[1]);
            double price = Double.parseDouble(a[2]);
            long vol = Long.parseLong(a[3]);
            batch.add(new StockTick(symbol, ts, price, vol));
            if (batch.size() >= 5000) {
                List<StockTick> toIngest = new ArrayList<>(batch);
                batch.clear();
                ingestBulk(toIngest, threads);
            }
        }
        if (!batch.isEmpty()) ingestBulk(batch, threads);
        br.close();
    }

    public List<StockTick> rangeQuery(String symbol, long startTs, long endTs) {
        ConcurrentSkipListMap<Long, StockTick> tree = index.get(symbol);
        if (tree == null) return Collections.emptyList();
        return new ArrayList<>(tree.subMap(startTs, true, endTs, true).values());
    }

    public NavigableMap<Long, Double> simpleMovingAverage(String symbol, long windowMs) {
        ConcurrentSkipListMap<Long, StockTick> tree = index.get(symbol);
        NavigableMap<Long, Double> result = new TreeMap<>();
        if (tree == null) return result;

        Deque<StockTick> window = new ArrayDeque<>();
        double sum = 0.0;
        for (StockTick tick : tree.values()) {
            window.addLast(tick);
            sum += tick.price;
            while (!window.isEmpty() && tick.ts - window.peekFirst().ts > windowMs) {
                sum -= window.removeFirst().price;
            }
            result.put(tick.ts, sum / window.size());
        }
        return result;
    }

    public List<Map.Entry<String, Double>> topKMovers(long startTs, long endTs, int k) {
        PriorityQueue<Map.Entry<String, Double>> minHeap = new PriorityQueue<>(Comparator.comparingDouble(Map.Entry::getValue));
        for (String symbol : index.keySet()) {
            ConcurrentSkipListMap<Long, StockTick> tree = index.get(symbol);
            if (tree == null) continue;
            Entry<Long, StockTick> startEntry = tree.floorEntry(startTs);
            Entry<Long, StockTick> endEntry = tree.floorEntry(endTs);
            if (startEntry == null || endEntry == null) continue;
            double startPrice = startEntry.getValue().price;
            double endPrice = endEntry.getValue().price;
            if (startPrice <= 0) continue;
            double pct = (endPrice - startPrice) / startPrice * 100.0;
            Map.Entry<String, Double> ent = new AbstractMap.SimpleEntry<>(symbol, pct);
            if (minHeap.size() < k) minHeap.add(ent);
            else if (pct > minHeap.peek().getValue()) {
                minHeap.poll();
                minHeap.add(ent);
            }
        }
        List<Map.Entry<String, Double>> out = new ArrayList<>(minHeap);
        out.sort((a,b) -> Double.compare(b.getValue(), a.getValue()));
        return out;
    }

    public Set<String> symbols() {
        return index.keySet();
    }
}
