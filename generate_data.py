#!/usr/bin/env python3
import argparse, random, csv
from datetime import datetime, timedelta

def gen_symbols(n):
    return [f"SYM{str(i).zfill(4)}" for i in range(n)]

def main():
    p = argparse.ArgumentParser()
    p.add_argument("--out", default="ticks.csv")
    p.add_argument("--symbols", type=int, default=100)
    p.add_argument("--days", type=int, default=7)
    p.add_argument("--rate", type=int, default=24)  # ticks per day per symbol
    args = p.parse_args()
    syms = gen_symbols(args.symbols)
    start_dt = datetime.utcnow() - timedelta(days=args.days)
    rows = 0
    with open(args.out, "w", newline='') as f:
        writer = csv.writer(f)
        writer.writerow(["symbol","ts","price","volume"])
        for s in syms:
            price = random.uniform(10, 200)
            ts = start_dt
            for d in range(args.days):
                for t in range(args.rate):
                    price *= random.uniform(0.999, 1.002)
                    ts_ms = int(ts.timestamp() * 1000)
                    vol = random.randint(1, 1000)
                    writer.writerow([s, ts_ms, round(price,4), vol])
                    rows += 1
                    ts += timedelta(minutes=60//max(1,args.rate))
    print(f"Wrote {rows} rows to {args.out}")

if __name__ == '__main__':
    main()
