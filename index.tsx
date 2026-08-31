import { useEffect, useMemo, useState } from "react";
import {
  Alert,
  Modal,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from "react-native";
import * as DocumentPicker from "expo-document-picker";
import { useAudioPlayer, useAudioPlayerStatus, setAudioModeAsync } from "expo-audio";
import AsyncStorage from "@react-native-async-storage/async-storage";
import { ScreenContainer } from "@/components/screen-container";
import { IconSymbol } from "@/components/ui/icon-symbol";
import { useColors } from "@/hooks/use-colors";

const QUICK_TIMERS = [15, 30, 45, 60];
const STORAGE_KEY = "sleepaudio.preferences.v1";

type SelectedAudio = { uri: string; name: string; duration?: number; mimeType?: string };
type TimerMode = "duration" | "endTime";

function formatTime(seconds: number) {
  const safe = Math.max(0, Math.floor(seconds || 0));
  const hours = Math.floor(safe / 3600);
  const minutes = Math.floor((safe % 3600) / 60);
  const secs = safe % 60;
  return hours > 0
    ? `${hours}:${String(minutes).padStart(2, "0")}:${String(secs).padStart(2, "0")}`
    : `${minutes}:${String(secs).padStart(2, "0")}`;
}

function getEndTimeFromClock(clock: string) {
  const [hours, minutes] = clock.split(":").map(Number);
  if (!Number.isFinite(hours) || !Number.isFinite(minutes) || hours > 23 || minutes > 59) return null;
  const now = new Date();
  const target = new Date(now);
  target.setHours(hours, minutes, 0, 0);
  if (target.getTime() <= now.getTime()) target.setDate(target.getDate() + 1);
  return target.getTime();
}

export default function HomeScreen() {
  const colors = useColors();
  const [audio, setAudio] = useState<SelectedAudio | null>(null);
  const [timerEnd, setTimerEnd] = useState<number | null>(null);
  const [timerMode, setTimerMode] = useState<TimerMode>("duration");
  const [fadeMinutes, setFadeMinutes] = useState(5);
  const [fadeEnabled, setFadeEnabled] = useState(true);
  const [remaining, setRemaining] = useState(0);
  const [timerVisible, setTimerVisible] = useState(false);
  const [customMinutes, setCustomMinutes] = useState("20");
  const [endClock, setEndClock] = useState("23:00");
  const [notice, setNotice] = useState<string | null>(null);

  const player = useAudioPlayer(audio ? { uri: audio.uri } : null);
  const status = useAudioPlayerStatus(player);
  const duration = status.duration || audio?.duration || 0;

  useEffect(() => {
    setAudioModeAsync({ playsInSilentMode: true, shouldPlayInBackground: true }).catch(() => undefined);
    AsyncStorage.getItem(STORAGE_KEY).then((value) => {
      if (!value) return;
      try {
        const saved = JSON.parse(value);
        if (saved.audio) setAudio(saved.audio);
        if (saved.fadeMinutes) setFadeMinutes(saved.fadeMinutes);
        if (typeof saved.fadeEnabled === "boolean") setFadeEnabled(saved.fadeEnabled);
      } catch {
        setNotice("Les préférences locales n’ont pas pu être restaurées.");
      }
    });
  }, []);

  useEffect(() => {
    AsyncStorage.setItem(STORAGE_KEY, JSON.stringify({ audio, fadeMinutes, fadeEnabled })).catch(() => undefined);
  }, [audio, fadeMinutes, fadeEnabled]);

  useEffect(() => {
    if (!timerEnd) {
      setRemaining(0);
      return;
    }
    const tick = () => setRemaining(Math.max(0, timerEnd - Date.now()));
    tick();
    const id = setInterval(tick, 1000);
    return () => clearInterval(id);
  }, [timerEnd]);

  useEffect(() => {
    if (!timerEnd || remaining > 0) return;
    player.pause();
    setTimerEnd(null);
    setNotice("La minuterie est terminée. La lecture a été arrêtée.");
  }, [remaining, timerEnd, player]);

  const progress = duration > 0 ? Math.min(1, status.currentTime / duration) : 0;
  const timerLabel = useMemo(() => {
    if (!timerEnd) return "Aucun minuteur actif";
    return `${formatTime(Math.ceil(remaining / 1000))} restantes`;
  }, [timerEnd, remaining]);

  const chooseAudio = async () => {
    try {
      const result = await DocumentPicker.getDocumentAsync({
        type: "audio/*",
        copyToCacheDirectory: true,
      });
      if (result.canceled) return;
      const asset = result.assets[0];
      setAudio({ uri: asset.uri, name: asset.name, duration: asset.size ? undefined : undefined, mimeType: asset.mimeType });
      setNotice("Audio prêt à être lu.");
    } catch {
      setNotice("Impossible d’ouvrir le sélecteur audio.");
    }
  };

  const confirmTimer = () => {
    const end = timerMode === "duration"
      ? Date.now() + Math.max(1, Number(customMinutes) || 0) * 60_000
      : getEndTimeFromClock(endClock);
    if (!end || end <= Date.now()) {
      Alert.alert("Minuterie invalide", "Choisissez une durée ou une heure de fin valide.");
      return;
    }
    setTimerEnd(end);
    setTimerVisible(false);
    setNotice("Minuterie enregistrée.");
  };

  const clearTimer = () => {
    setTimerEnd(null);
    setTimerVisible(false);
    setNotice("Minuterie désactivée.");
  };

  return (
    <ScreenContainer containerClassName="bg-background" className="px-5 pt-4">
      <ScrollView contentContainerStyle={styles.content} showsVerticalScrollIndicator={false}>
        <View style={styles.header}>
          <View>
            <Text style={[styles.eyebrow, { color: colors.tint }]}>LECTEUR LOCAL</Text>
            <Text style={[styles.title, { color: colors.text }]}>SleepAudio</Text>
          </View>
          <View style={[styles.statusDot, { backgroundColor: colors.tint }]} />
        </View>

        <Text style={[styles.subtitle, { color: colors.muted }]}>Un espace calme pour écouter, puis laisser la nuit faire son travail.</Text>

        <View style={[styles.audioCard, { backgroundColor: colors.surface, borderColor: colors.border }]}>
          <View style={[styles.art, { backgroundColor: colors.tint + "22" }]}>
            <IconSymbol name="moon.fill" size={34} color={colors.tint} />
          </View>
          <View style={styles.audioInfo}>
            <Text style={[styles.cardLabel, { color: colors.muted }]}>AUDIO SÉLECTIONNÉ</Text>
            <Text style={[styles.audioName, { color: colors.text }]} numberOfLines={2}>{audio?.name ?? "Aucun audio pour le moment"}</Text>
            <Text style={[styles.audioMeta, { color: colors.muted }]}>{audio ? "Fichier local prêt" : "Choisissez un fichier depuis votre appareil"}</Text>
          </View>
          <Pressable onPress={chooseAudio} style={({ pressed }) => [styles.iconButton, { borderColor: colors.border }, pressed && styles.pressed]} accessibilityLabel="Choisir un audio">
            <IconSymbol name="plus" size={22} color={colors.tint} />
          </Pressable>
        </View>

        <View style={[styles.timerCard, { backgroundColor: colors.tint + "16", borderColor: colors.tint + "55" }]}>
          <View style={styles.rowBetween}>
            <View style={styles.rowGap}>
              <IconSymbol name="timer" size={20} color={colors.tint} />
              <Text style={[styles.timerTitle, { color: colors.text }]}>Arrêt automatique</Text>
            </View>
            <Pressable onPress={() => setTimerVisible(true)} style={({ pressed }) => [styles.smallAction, pressed && styles.pressed]}>
              <Text style={{ color: colors.tint, fontWeight: "700" }}>Modifier</Text>
            </Pressable>
          </View>
          <Text style={[styles.timerValue, { color: colors.text }]}>{timerLabel}</Text>
          <View style={styles.quickRow}>
            {QUICK_TIMERS.map((minutes) => (
              <Pressable key={minutes} onPress={() => { setTimerEnd(Date.now() + minutes * 60_000); setNotice(`${minutes} minutes programmées.`); }} style={({ pressed }) => [styles.quickButton, { backgroundColor: colors.surface, borderColor: colors.border }, pressed && styles.pressed]}>
                <Text style={{ color: colors.text, fontWeight: "700" }}>{minutes} min</Text>
              </Pressable>
            ))}
          </View>
        </View>

        <View style={styles.sectionHeader}>
          <Text style={[styles.sectionTitle, { color: colors.text }]}>Lecture</Text>
          <Text style={[styles.sectionHint, { color: colors.muted }]}>{status.playing ? "En cours" : "En pause"}</Text>
        </View>
        <View style={[styles.playerCard, { backgroundColor: colors.surface, borderColor: colors.border }]}>
          <View style={styles.progressHeader}>
            <Text style={[styles.timeText, { color: colors.muted }]}>{formatTime(status.currentTime)}</Text>
            <Text style={[styles.timeText, { color: colors.muted }]}>{formatTime(duration)}</Text>
          </View>
          <View style={[styles.progressTrack, { backgroundColor: colors.border }]}>
            <View style={[styles.progressFill, { backgroundColor: colors.tint, width: `${progress * 100}%` }]} />
          </View>
          <View style={styles.controls}>
            <Pressable onPress={() => { player.pause(); player.seekTo(0); }} style={({ pressed }) => [styles.secondaryControl, pressed && styles.pressed]}>
              <IconSymbol name="stop" size={22} color={colors.muted} />
            </Pressable>
            <Pressable onPress={() => player.seekTo(Math.max(0, status.currentTime - 15))} style={({ pressed }) => [styles.secondaryControl, pressed && styles.pressed]}>
              <IconSymbol name="replay-10" size={22} color={colors.muted} />
            </Pressable>
            <Pressable onPress={() => audio ? (status.playing ? player.pause() : player.play()) : chooseAudio()} style={({ pressed }) => [styles.playButton, { backgroundColor: colors.tint }, pressed && styles.playPressed]} accessibilityLabel={status.playing ? "Mettre en pause" : "Lire"}>
              <IconSymbol name={status.playing ? "pause" : "play-arrow"} size={28} color="#0B1020" />
            </Pressable>
            <Pressable onPress={() => player.seekTo(Math.min(duration, status.currentTime + 15))} style={({ pressed }) => [styles.secondaryControl, pressed && styles.pressed]}>
              <IconSymbol name="forward-10" size={22} color={colors.muted} />
            </Pressable>
          </View>
        </View>

        <View style={styles.sectionHeader}>
          <Text style={[styles.sectionTitle, { color: colors.text }]}>Fondu de fin</Text>
          <Pressable onPress={() => setFadeEnabled((value) => !value)} style={({ pressed }) => [styles.toggle, { backgroundColor: fadeEnabled ? colors.tint : colors.border }, pressed && styles.pressed]}>
            <View style={[styles.toggleThumb, { backgroundColor: fadeEnabled ? "#0B1020" : colors.muted, alignSelf: fadeEnabled ? "flex-end" : "flex-start" }]} />
          </Pressable>
        </View>
        <View style={[styles.fadeCard, { backgroundColor: colors.surface, borderColor: colors.border }]}>
          <Text style={[styles.fadeCopy, { color: colors.muted }]}>Réduction progressive du volume avant l’arrêt.</Text>
          <View style={styles.fadeOptions}>
            {[3, 5, 10].map((minutes) => (
              <Pressable key={minutes} onPress={() => setFadeMinutes(minutes)} style={({ pressed }) => [styles.fadeOption, { borderColor: fadeMinutes === minutes ? colors.tint : colors.border, backgroundColor: fadeMinutes === minutes ? colors.tint + "1A" : "transparent" }, pressed && styles.pressed]}>
                <Text style={{ color: fadeMinutes === minutes ? colors.tint : colors.muted, fontWeight: "700" }}>{minutes} min</Text>
              </Pressable>
            ))}
          </View>
        </View>

        {notice ? <Text style={[styles.notice, { color: colors.muted }]}>{notice}</Text> : null}
      </ScrollView>

      <Modal visible={timerVisible} transparent animationType="slide" onRequestClose={() => setTimerVisible(false)}>
        <View style={styles.modalBackdrop}>
          <View style={[styles.modalCard, { backgroundColor: colors.surface }]}>
            <View style={styles.rowBetween}>
              <Text style={[styles.modalTitle, { color: colors.text }]}>Arrêt automatique</Text>
              <Pressable onPress={() => setTimerVisible(false)}><IconSymbol name="close" size={22} color={colors.muted} /></Pressable>
            </View>
            <View style={styles.modeRow}>
              <Pressable onPress={() => setTimerMode("duration")} style={[styles.modeButton, { backgroundColor: timerMode === "duration" ? colors.tint : colors.background }]}><Text style={{ color: timerMode === "duration" ? "#0B1020" : colors.muted, fontWeight: "700" }}>Durée</Text></Pressable>
              <Pressable onPress={() => setTimerMode("endTime")} style={[styles.modeButton, { backgroundColor: timerMode === "endTime" ? colors.tint : colors.background }]}><Text style={{ color: timerMode === "endTime" ? "#0B1020" : colors.muted, fontWeight: "700" }}>Heure de fin</Text></Pressable>
            </View>
            {timerMode === "duration" ? <>
              <Text style={[styles.fieldLabel, { color: colors.muted }]}>Durée en minutes</Text>
              <TextInput value={customMinutes} onChangeText={setCustomMinutes} keyboardType="number-pad" style={[styles.input, { color: colors.text, borderColor: colors.border, backgroundColor: colors.background }]} />
              <View style={styles.quickRow}>{QUICK_TIMERS.map((minutes) => <Pressable key={minutes} onPress={() => setCustomMinutes(String(minutes))} style={[styles.quickButton, { backgroundColor: colors.background, borderColor: colors.border }]}><Text style={{ color: colors.text }}>{minutes}</Text></Pressable>)}</View>
            </> : <>
              <Text style={[styles.fieldLabel, { color: colors.muted }]}>Heure locale d’arrêt, le lendemain si nécessaire</Text>
              <TextInput value={endClock} onChangeText={setEndClock} placeholder="23:00" placeholderTextColor={colors.muted} keyboardType="numbers-and-punctuation" style={[styles.input, { color: colors.text, borderColor: colors.border, backgroundColor: colors.background }]} />
            </>}
            <Pressable onPress={confirmTimer} style={({ pressed }) => [styles.confirmButton, { backgroundColor: colors.tint }, pressed && styles.playPressed]}><Text style={styles.confirmText}>Confirmer la minuterie</Text></Pressable>
            {timerEnd ? <Pressable onPress={clearTimer} style={styles.clearButton}><Text style={{ color: colors.error, fontWeight: "700" }}>Désactiver</Text></Pressable> : null}
          </View>
        </View>
      </Modal>
    </ScreenContainer>
  );
}

const styles: any = StyleSheet.create({
  content: { paddingBottom: 36, gap: 18 },
  header: { flexDirection: "row", justifyContent: "space-between", alignItems: "center" },
  eyebrow: { fontSize: 11, fontWeight: "800", letterSpacing: 1.4 },
  title: { fontSize: 31, fontWeight: "800", letterSpacing: -1 },
  subtitle: { fontSize: 15, lineHeight: 22, maxWidth: 340 },
  statusDot: { width: 10, height: 10, borderRadius: 5, shadowOpacity: 0.5, shadowRadius: 8 },
  audioCard: { borderWidth: 1, borderRadius: 24, padding: 16, flexDirection: "row", alignItems: "center", gap: 13 },
  art: { width: 60, height: 60, borderRadius: 18, alignItems: "center", justifyContent: "center" },
  audioInfo: { flex: 1, gap: 4 },
  cardLabel: { fontSize: 10, fontWeight: "800", letterSpacing: 1 },
  audioName: { fontSize: 17, fontWeight: "700" },
  audioMeta: { fontSize: 12 },
  iconButton: { width: 42, height: 42, borderRadius: 21, borderWidth: 1, alignItems: "center", justifyContent: "center" },
  timerCard: { borderRadius: 24, borderWidth: 1, padding: 17, gap: 12 },
  rowBetween: { flexDirection: "row", justifyContent: "space-between", alignItems: "center" },
  rowGap: { flexDirection: "row", alignItems: "center", gap: 8 },
  timerTitle: { fontSize: 15, fontWeight: "700" },
  smallAction: { paddingVertical: 4, paddingHorizontal: 2 },
  timerValue: { fontSize: 23, fontWeight: "800" },
  quickRow: { flexDirection: "row", gap: 8 },
  quickButton: { flex: 1, minHeight: 38, borderRadius: 12, borderWidth: 1, alignItems: "center", justifyContent: "center" },
  sectionHeader: { flexDirection: "row", justifyContent: "space-between", alignItems: "center", marginTop: 2 },
  sectionTitle: { fontSize: 18, fontWeight: "800" },
  sectionHint: { fontSize: 13 },
  playerCard: { borderWidth: 1, borderRadius: 24, padding: 18, gap: 16 },
  progressHeader: { flexDirection: "row", justifyContent: "space-between" },
  timeText: { fontSize: 12, fontVariant: ["tabular-nums"] },
  progressTrack: { height: 6, borderRadius: 3, overflow: "hidden" },
  progressFill: { height: "100%", borderRadius: 3 },
  controls: { flexDirection: "row", justifyContent: "center", alignItems: "center", gap: 26 },
  secondaryControl: { width: 42, height: 42, alignItems: "center", justifyContent: "center" },
  playButton: { width: 66, height: 66, borderRadius: 33, alignItems: "center", justifyContent: "center", shadowOpacity: 0.25, shadowRadius: 12, elevation: 5 },
  fadeCard: { borderWidth: 1, borderRadius: 20, padding: 16, gap: 13 },
  fadeCopy: { fontSize: 13, lineHeight: 19 },
  fadeOptions: { flexDirection: "row", gap: 8 },
  fadeOption: { flex: 1, borderWidth: 1, borderRadius: 11, paddingVertical: 10, alignItems: "center" },
  toggle: { width: 48, height: 28, borderRadius: 14, padding: 3, justifyContent: "center" },
  toggleThumb: { width: 22, height: 22, borderRadius: 11 },
  notice: { textAlign: "center", fontSize: 12, lineHeight: 18 },
  pressed: { opacity: 0.72 },
  playPressed: { transform: [{ scale: 0.97 }], opacity: 0.9 },
  modalBackdrop: { flex: 1, backgroundColor: "rgba(5, 8, 20, 0.7)", justifyContent: "flex-end" },
  modalCard: { borderTopLeftRadius: 28, borderTopRightRadius: 28, padding: 22, gap: 16 },
  modalTitle: { fontSize: 22, fontWeight: "800" },
  modeRow: { flexDirection: "row", gap: 8 },
  modeButton: { flex: 1, borderRadius: 12, paddingVertical: 12, alignItems: "center" },
  fieldLabel: { fontSize: 13, fontWeight: "700" },
  input: { height: 50, borderWidth: 1, borderRadius: 14, paddingHorizontal: 15, fontSize: 18 },
  confirmButton: { borderRadius: 15, minHeight: 52, alignItems: "center", justifyContent: "center", marginTop: 4 },
  confirmText: { color: "#0B1020", fontSize: 15, fontWeight: "800" },
  clearButton: { alignItems: "center", paddingVertical: 10 },
});
