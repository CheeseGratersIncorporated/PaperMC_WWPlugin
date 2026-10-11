package io.github.gegagegagega.werewolf.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

final class RecordingListener implements GameListener {
    final List<GamePhase> phases = new ArrayList<>();
    final List<String> broadcasts = new ArrayList<>();
    final Map<String, List<String>> tells = new HashMap<>();
    List<String> results;

    @Override
    public void onPhaseStarted(GamePhase phase) {
        phases.add(phase);
    }

    @Override
    public void broadcast(Component message) {
        broadcasts.add(plain(message));
    }

    @Override
    public void tell(WWPlayer player, Component message) {
        tells.computeIfAbsent(player.getName(), k -> new ArrayList<>()).add(plain(message));
    }

    @Override
    public void onGameEnded(List<Component> lines) {
        results = lines.stream().map(RecordingListener::plain).toList();
    }

    static String plain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }
}
