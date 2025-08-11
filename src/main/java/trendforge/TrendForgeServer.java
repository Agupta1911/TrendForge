package trendforge;

import static spark.Spark.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.*;

public class TrendForgeServer {
    private static final TrendForgeEngine engine = new TrendForgeEngine();
    private static final Gson gson = new Gson();

    public static void main(String[] args) throws Exception {
        port(4567);
        // simple health endpoint
        get("/health", (req, res) -> "OK");

        post("/ingest", (req, res) -> {
            res.type("application/json");
            Type t = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> body = gson.fromJson(req.body(), t);
            try {
                String symbol = (String) body.get("symbol");
                double price = ((Number)body.get("price")).doubleValue();
                long ts = ((Number)body.get("ts")).longValue();
                long volume = ((Number)body.get("volume")).longValue();
                StockTick tick = new StockTick(symbol, ts, price, volume);
                engine.ingestTick(tick);
                return gson.toJson(Map.of("status","ok"));
            } catch (Exception e) {
                res.status(400);
                return gson.toJson(Map.of("status","error","msg", e.getMessage()));
            }
        });

        get("/range", (req, res) -> {
            res.type("application/json");
            String symbol = req.queryParams("symbol");
            long start = Long.parseLong(req.queryParams("start"));
            long end = Long.parseLong(req.queryParams("end"));
            return gson.toJson(engine.rangeQuery(symbol, start, end));
        });

        get("/topk", (req, res) -> {
            res.type("application/json");
            long start = Long.parseLong(req.queryParams("start"));
            long end = Long.parseLong(req.queryParams("end"));
            int k = Integer.parseInt(req.queryParams("k"));
            return gson.toJson(engine.topKMovers(start, end, k));
        });

        get("/sma", (req, res) -> {
            res.type("application/json");
            String symbol = req.queryParams("symbol");
            long window = Long.parseLong(req.queryParams("windowMs"));
            return gson.toJson(engine.simpleMovingAverage(symbol, window));
        });

        get("/symbols", (req, res) -> {
            res.type("application/json");
            return gson.toJson(engine.symbols());
        });

        // optional: load CSV background if provided via arg[0]
        if (args.length > 0) {
            String csv = args[0];
            int threads = 8;
            System.out.println("Loading CSV in background: " + csv);
            new Thread(() -> {
                try {
                    engine.loadCsvMultiThreaded(csv, threads);
                    System.out.println("CSV load complete. Symbols: " + engine.symbols().size());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}
