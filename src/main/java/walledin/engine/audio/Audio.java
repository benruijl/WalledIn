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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

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
    /** Reference to only instance of Audio. */
    private static Audio ref = null;

    /** Map of sample name to sample. */
    private Map<String, Sample> samples;

    /**
     * Private constructor.
     */
    private Audio() {
        samples = new HashMap<String, Sample>();
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
     * Loads a Wave sample.
     * @param name Name of resource
     * @param url URL of resource
     * @return Audio sample or null on failure
     */
    public Sample loadWaveSample(final String name, final URL url) {
        AudioInputStream stream;
        try {
            stream = AudioSystem.getAudioInputStream(url);

            // read the stream
            byte[] data = new byte[(int) stream.getFrameLength()];
            stream.read(data);

            Sample sample = new Sample(data, stream.getFormat(), true);
            samples.put(name, sample);
            return sample;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Loads an Ogg sample.
     * @param name Name of the sample
     * @param url URL of the sampel
     * @return Audio sample or null on failure
     */
    public Sample loadOggSample(final String name, final URL url) {
        try {
            OggDecoder decoder = new OggDecoder();
            OggData ogg = decoder.getData(url.openStream());
            AudioFormat format = new AudioFormat(ogg.getRate(), 16,
                    ogg.getChannels(), true, false);

            Sample sample = new Sample(ogg.getData(), format, false);
            samples.put(name, sample);
            return sample;

        } catch (IOException e) {
            LOG.info("Could not load file: " + url.getFile());
            e.printStackTrace();
        }

        return null;
    }


    /**
     * Plays a sample.
     * @param name Sample name
     */
    public void playSample(final String name) {
        try {
            if (!samples.containsKey(name)) {
                return;
            }
            
            Sample sample = samples.get(name);
            sample.play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
