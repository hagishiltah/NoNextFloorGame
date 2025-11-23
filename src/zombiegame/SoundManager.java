package zombiegame;
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SoundManager {

    private static Map<String, Clip> clips = new HashMap<>();

    // 사운드 파일 로드
    public static void loadSounds() {
        loadClip("bgm", "sounds/bgm.wav");
        loadClip("game_bg", "sounds/game_bg.wav");
        loadClip("game_bg2", "sounds/game_bg2.wav");
        loadClip("game_bg3", "sounds/game_bg3.wav");
        loadClip("zombie1", "sounds/zombie1.wav");
        loadClip("zombie23", "sounds/zombie23.wav");
        loadClip("button", "sounds/button.wav");
        loadClip("elevator", "sounds/elevator.wav");
        loadClip("gameover", "sounds/gameover.wav");
        loadClip("hit", "sounds/hit.wav");
        loadClip("pistol", "sounds/pistol.wav");
        loadClip("shotgun", "sounds/shotgun.wav");
        loadClip("start", "sounds/start.wav");
        loadClip("zombie_die", "sounds/zombie_die.wav");
    }

    private static void loadClip(String key, String filePath) {
        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(new File(filePath));
            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            clips.put(key, clip);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    // 반복 재생용
    public static void playLoop(String key, float volume) {
        Clip clip = clips.get(key);
        if (clip != null) {
            if (clip.isRunning()) clip.stop();
            clip.setFramePosition(0);
            setVolume(clip, volume);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    // 한 번 재생용
    public static void play(String key, float volume) {
        Clip clip = clips.get(key);
        if (clip != null) {
            if (clip.isRunning()) clip.stop();
            clip.setFramePosition(0);
            setVolume(clip, volume);
            clip.start();
        }
    }

    // 정지
    public static void stop(String key) {
        Clip clip = clips.get(key);
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }

    // 볼륨 설정
    private static void setVolume(Clip clip, float volume) {
        FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        float dB = (float) (Math.log10(volume <= 0 ? 0.0001 : volume) * 20);
        gainControl.setValue(dB);
    }
}
