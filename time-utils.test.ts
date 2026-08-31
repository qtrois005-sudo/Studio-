import { describe, expect, it } from "vitest";
import { endTimeForClock, remainingMillis } from "../lib/time-utils";

describe("time utilities", () => {
  it("never returns a negative remaining duration", () => {
    expect(remainingMillis(1_000, 2_000)).toBe(0);
    expect(remainingMillis(5_000, 2_000)).toBe(3_000);
  });

  it("moves an already-passed end time to the next day", () => {
    const now = new Date("2026-08-28T23:40:00");
    expect(endTimeForClock("23:00", now)?.toISOString()).toBe("2026-08-29T23:00:00.000Z");
  });

  it("rejects invalid clock values", () => {
    expect(endTimeForClock("25:70", new Date())).toBeNull();
    expect(endTimeForClock("bad", new Date())).toBeNull();
  });
});
