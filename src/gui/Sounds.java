package gui;

import javax.sound.sampled.*;
import javax.sound.sampled.LineEvent.Type;
import java.io.File;
import java.io.IOException;

public enum Sounds {
    CHECK("check.wav"),
    MOVE("move.wav"),
    CAPTURE("capture.wav"),
    CASTLE("castle.wav"),
    PROMOTION("promotion.wav"),
    CHECKMATE("game-end.wav"),
    DRAW("game-end.wav");

    private static final String PATH = "resources/skins/default/sounds/";


    private final File path;

    Sounds(String path) {
        this.path = new File(PATH + path);
    }

    public void play() {
        var t = new Thread(() -> {
            try {
                playClip(this.path);
            } catch (IOException | LineUnavailableException |
                     UnsupportedAudioFileException | InterruptedException e) {
                System.err.println("Error playing sound: " + this.path + "\n" + e);
            }
        });
        t.start();
    }


    private static void playClip(File clipFile) throws IOException,
            UnsupportedAudioFileException, LineUnavailableException, InterruptedException {
        class AudioListener implements LineListener {
            private boolean done = false;

            public synchronized void wait_() throws InterruptedException {
                while (!done) {
                    wait();
                }
            }

            @Override
            public synchronized void update(LineEvent event) {
                Type eventType = event.getType();
                if (eventType == Type.STOP || eventType == Type.CLOSE) {
                    done = true;
                    notifyAll();
                }
            }
        }

        AudioListener listener = new AudioListener();
        try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(clipFile)) {
            Clip clip = AudioSystem.getClip();
            try (clip) {
                clip.addLineListener(listener);
                clip.open(audioInputStream);
                clip.start();
                listener.wait_();
            }
        }
    }
}
