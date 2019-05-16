import com.intellij.openapi.diagnostic.Logger;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class AudioPlayerListener implements LineListener {

    private static final Logger LOGGER = Logger.getInstance(AudioPlayerListener.class);

    private static final String CUSTOM_PATH = System.getProperty("plugin.nyancat.path");
    private static final String DELAY = System.getProperty("plugin.nyancat.delay");

    private boolean playCompleted;

    void play() {
        Clip audioClip = null;
        try {
            TimeUnit.SECONDS.sleep(DELAY != null ? Long.parseLong(DELAY) : 10);
            AudioInputStream audioStream = createAudioStream();
            AudioFormat format = audioStream.getFormat();
            DataLine.Info info = new DataLine.Info(Clip.class, format);
            audioClip = (Clip) AudioSystem.getLine(info);
            audioClip.addLineListener(this);
            audioClip.open(audioStream);
            playAudioOnRepeat(audioClip);
        } catch (LineUnavailableException | IOException | UnsupportedAudioFileException e) {
            LOGGER.error(e);
        } catch (InterruptedException ex) {
            LOGGER.info("Interrupt playing the audio file.");
        } finally {
            Optional.ofNullable(audioClip).ifPresent(Clip::close);
        }
    }

    private void playAudioOnRepeat(Clip audioClip) throws InterruptedException {
        while (true) {
            playCompleted = false;
            audioClip.setMicrosecondPosition(0);
            audioClip.start();
            //wait for the end of the audio file
            while (!playCompleted && !Thread.currentThread().isInterrupted()) {
                TimeUnit.MILLISECONDS.sleep(100);
            }
        }
    }

    private AudioInputStream createAudioStream() throws UnsupportedAudioFileException, IOException {
        return CUSTOM_PATH != null
                ? AudioSystem.getAudioInputStream(new File(CUSTOM_PATH))
                : AudioSystem.getAudioInputStream(getClass().getResourceAsStream("nyan.wav"));
    }

    @Override
    public void update(LineEvent event) {
        LineEvent.Type type = event.getType();

        if (type == LineEvent.Type.STOP) {
            playCompleted = true;
        }
        LOGGER.info("Playback status: " + type);
    }

}
