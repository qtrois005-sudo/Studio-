export function remainingMillis(endTimeMillis: number, nowMillis = Date.now()) {
  return Math.max(0, endTimeMillis - nowMillis);
}

export function endTimeForClock(clock: string, now: Date) {
  const [hours, minutes] = clock.split(":").map(Number);
  if (!Number.isInteger(hours) || !Number.isInteger(minutes) || hours < 0 || hours > 23 || minutes < 0 || minutes > 59) return null;
  const target = new Date(now);
  target.setHours(hours, minutes, 0, 0);
  if (target.getTime() <= now.getTime()) target.setDate(target.getDate() + 1);
  return target;
}
