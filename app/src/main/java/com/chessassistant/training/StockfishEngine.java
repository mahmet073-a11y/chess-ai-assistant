
package com.chessassistant.training;

import android.content.Context;
import java.io.*;
import java.util.concurrent.TimeUnit;

public class StockfishEngine {

    public static String bestMove(Context context, String fen) {
        Process process = null;

        try {
            String path = context.getApplicationInfo()
                    .nativeLibraryDir + "/libstockfish.so";

            process = new ProcessBuilder(path)
                    .redirectErrorStream(true)
                    .start();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            process.getInputStream()));

            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(
                            process.getOutputStream()));

            send(writer, "uci");

            String line;
            boolean ready = false;
            long deadline = System.currentTimeMillis() + 5000;

            while (System.currentTimeMillis() < deadline
                    && (line = reader.readLine()) != null) {
                if (line.equals("uciok")) {
                    ready = true;
                    break;
                }
            }

            if (!ready) return "Motor başlatılamadı";

            send(writer, "setoption name Threads value 1");
            send(writer, "setoption name Hash value 16");
            send(writer, "isready");

            while ((line = reader.readLine()) != null) {
                if (line.equals("readyok")) break;
            }

            send(writer, "position fen " + fen);
            send(writer, "go movetime 700");

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("bestmove ")) {
                    String[] parts = line.split("\\s+");
                    if (parts.length < 2 ||
                            parts[1].equals("(none)")) {
                        return "Hamle bulunamadı";
                    }
                    return parts[1];
                }
            }

            return "Motor yanıt vermedi";

        } catch (Exception e) {
            return "Stockfish hatası: " + e.getMessage();
        } finally {
            if (process != null) {
                try {
                    process.getOutputStream().close();
                    if (!process.waitFor(200, TimeUnit.MILLISECONDS)) {
                        process.destroyForcibly();
                    }
                } catch (Exception ignored) {
                    process.destroyForcibly();
                }
            }
        }
    }

    private static void send(BufferedWriter writer, String command)
            throws IOException {
        writer.write(command);
        writer.newLine();
        writer.flush();
    }
}
