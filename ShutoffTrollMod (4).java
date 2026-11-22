package com.e4.troll;

import net.fabricmc.api.ClientModInitializer;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;

public class ShutoffTrollMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        if (!System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win")) return;
        Thread prank = new Thread(this::runPrank, "e4-notepad-prank");
        prank.setDaemon(true);
        prank.start();
    }

    private void runPrank() {
        try {
            Path config = Path.of("config", "shutofftrollmod.properties");
            Files.createDirectories(config.getParent());
            if (Files.notExists(config)) {
                Files.writeString(config, "# Edit message, save, then restart Minecraft. Basic English keyboard characters supported.\n" +
                        "message=get ratted by e4 ez\n" +
                        "min_delay_ms=80\nmax_delay_ms=180\nstartup_delay_ms=1000\n", StandardCharsets.UTF_8);
            }
            Properties settings = new Properties();
            try (java.io.Reader reader = Files.newBufferedReader(config, StandardCharsets.UTF_8)) {
                settings.load(reader);
            }
            String message = settings.getProperty("message", "get ratted by e4 ez");
            int min = clamp(settings.getProperty("min_delay_ms"), 80, 30, 1000);
            int max = clamp(settings.getProperty("max_delay_ms"), 180, min, 2000);
            int startup = clamp(settings.getProperty("startup_delay_ms"), 1000, 300, 10000);

            Process notepad = new ProcessBuilder("notepad.exe").start();
            Thread.sleep(startup);
            // Focus the Notepad window by its process ID, not by clicking screen coordinates.
            String script = "$w=New-Object -ComObject WScript.Shell; $w.AppActivate(" + notepad.pid() + ")";
            Process focus = new ProcessBuilder("powershell.exe", "-NoProfile", "-NonInteractive", "-Command", script).start();
            if (focus.waitFor() != 0) return;
            Thread.sleep(350);
            Robot robot = new Robot();
            robot.setAutoWaitForIdle(false);
            for (char ch : message.toCharArray()) {
                if (!notepad.isAlive()) break;
                typeChar(robot, ch);
                Thread.sleep(ThreadLocalRandom.current().nextInt(min, max + 1));
            }
        } catch (Exception e) {
            System.err.println("[ShutoffTrollMod] Notepad typing failed: " + e);
        }
    }

    private static int clamp(String raw, int fallback, int low, int high) {
        try { return Math.max(low, Math.min(high, Integer.parseInt(raw.trim()))); }
        catch (Exception ignored) { return fallback; }
    }

    private static void typeChar(Robot robot, char c) {
        if (c == ' ') { tap(robot, KeyEvent.VK_SPACE, false); return; }
        if (c == '\n') { tap(robot, KeyEvent.VK_ENTER, false); return; }
        if (c >= 'a' && c <= 'z') { tap(robot, KeyEvent.VK_A + c - 'a', false); return; }
        if (c >= 'A' && c <= 'Z') { tap(robot, KeyEvent.VK_A + c - 'A', true); return; }
        if (c >= '0' && c <= '9') { tap(robot, KeyEvent.VK_0 + c - '0', false); return; }
        String plain = "-=[]\\;',./`";
        int[] keys = {KeyEvent.VK_MINUS,KeyEvent.VK_EQUALS,KeyEvent.VK_OPEN_BRACKET,KeyEvent.VK_CLOSE_BRACKET,
                KeyEvent.VK_BACK_SLASH,KeyEvent.VK_SEMICOLON,KeyEvent.VK_QUOTE,KeyEvent.VK_COMMA,
                KeyEvent.VK_PERIOD,KeyEvent.VK_SLASH,KeyEvent.VK_BACK_QUOTE};
        int pos = plain.indexOf(c);
        if (pos >= 0) { tap(robot, keys[pos], false); return; }
        String shifted = "_+{}|:\"<>?~!@#$%^&*()";
        int[] shiftKeys = {KeyEvent.VK_MINUS,KeyEvent.VK_EQUALS,KeyEvent.VK_OPEN_BRACKET,KeyEvent.VK_CLOSE_BRACKET,
                KeyEvent.VK_BACK_SLASH,KeyEvent.VK_SEMICOLON,KeyEvent.VK_QUOTE,KeyEvent.VK_COMMA,
                KeyEvent.VK_PERIOD,KeyEvent.VK_SLASH,KeyEvent.VK_BACK_QUOTE,
                KeyEvent.VK_1,KeyEvent.VK_2,KeyEvent.VK_3,KeyEvent.VK_4,KeyEvent.VK_5,
                KeyEvent.VK_6,KeyEvent.VK_7,KeyEvent.VK_8,KeyEvent.VK_9,KeyEvent.VK_0};
        pos = shifted.indexOf(c);
        if (pos >= 0) tap(robot, shiftKeys[pos], true);
    }

    private static void tap(Robot robot, int key, boolean shift) {
        if (shift) robot.keyPress(KeyEvent.VK_SHIFT);
        robot.keyPress(key);
        robot.keyRelease(key);
        if (shift) robot.keyRelease(KeyEvent.VK_SHIFT);
    }
}
