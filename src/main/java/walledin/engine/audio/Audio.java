/*  Copyright 2010 Ben Ruijl, Wouter Smeenk

This file is part of Walled In.

Walled In is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3, or (at your option)
any later version.

Walled In is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with Walled In; see the file LICENSE.  If not, write to the
Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA.

 */
package walledin.engine.audio;

import java.io.IOException;
import java.net.URL;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import org.apache.log4j.Logger;

/**
 * A singleton audio manager.
 * 
 * @author Ben Ruijl
 * 
 */
public final class Audio {
    /** Logger. */
    private static final Logger LOG = Logger.getLogger(Audio.class);
    /** Reference to only instance of Adio. */
    private static Audio ref = null;

    /**
     * Private constructor.
     */
    private Audio() {

    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    /**
     * Gets the only instance of Audio.
     * 
     * @return Audio
     */
    public static Audio getInstance() {
        if (ref == null) {
            ref = new Audio();
        }

        return ref;
    }

    /**
     * Plays a wave file.
     * 
     * @param url
     *            URL of file
     */
    public void playSound(final URL url) {
        try {
            AudioInputStream stream = AudioSystem.getAudioInputStream(url);

            DataLine.Info info = new DataLine.Info(Clip.class,
                    stream.getFormat());
            Clip clip = (Clip) AudioSystem.getLine(info);

            clip.open(stream);
            clip.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void playOgg(final URL url) {
        Runnable r = new Runnable() {
            public void run() {
                playOggThread(url);
            }
        };

        Thread t = new Thread(r);
        t.start();
    }

    private void playOggThread(final URL url) {
        OggDecoder decoder = new OggDecoder();
        try {
            OggData ogg = decoder.getData(url.openStream());
            AudioFormat format = new AudioFormat(ogg.getRate(), 16,
                    ogg.getChannels(), true, false);
            DataLine.Info datalineInfo = new DataLine.Info(
                    SourceDataLine.class, format, AudioSystem.NOT_SPECIFIED);
            SourceDataLine outputLine;
            try {
                outputLine = (SourceDataLine) AudioSystem.getLine(datalineInfo);
                outputLine.open(format);
                outputLine.start();
                // if (ogg.getData().hasArray()) {
                byte[] data = new byte[ogg.getData().capacity()];
                ogg.getData().get(data);
                outputLine.write(data, 0, ogg.getData().capacity());
                // }
            } catch (LineUnavailableException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

        } catch (IOException e) {
            LOG.info("Could not load file: " + url.getFile());
            e.printStackTrace();
        }
    }
}
