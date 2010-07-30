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

import java.util.LinkedList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;

/**
 * This class holds the information for a sound sample. It can generate multiple
 * clips, if allowed, to play the same sample multiple times.
 * 
 * @author Ben Ruijl
 * 
 */
public final class Sample {
    /** PCM data. */
    private byte[] data;
    /** Format. */
    private final AudioFormat format;
    /**
     * Clips. If the clips is only allowed to play once, this list will have one
     * item max.
     */
    private List<Clip> clips;
    /** Play multiple times? */
    private boolean playMultipleTimes;

    /**
     * Creates a new sample.
     * 
     * @param data
     *            PCM data
     * @param format
     *            Audio format
     * @param playMultipleTimes
     *            If set, this sample can be played multiple times at once.
     */
    public Sample(final byte[] data, final AudioFormat format,
            final boolean playMultipleTimes) {
        super();
        this.data = data;
        this.format = format;
        this.playMultipleTimes = playMultipleTimes;
        clips = new LinkedList<Clip>();
    }

    /**
     * Generates a clip from the data.
     * 
     * @throws LineUnavailableException
     *             This error gets thrown when too many lines are in use.
     * @return Generated clip
     */
    private Clip generateClip() throws LineUnavailableException {
        DataLine.Info info = new DataLine.Info(Clip.class, format,
                AudioSystem.NOT_SPECIFIED);
        Clip clip = (Clip) AudioSystem.getLine(info);

        clip.addLineListener(new LineListener() {

            @Override
            public void update(final LineEvent arg0) {
                if (arg0.getType() == LineEvent.Type.CLOSE) {
                    clips.remove((Clip) arg0.getLine());
                }
            }
        });

        clips.add((Clip) AudioSystem.getLine(info));
        return clip;
    }

    /**
     * Plays this sample. If this sample is already playing and
     * <code>playMultipleTimes</code> is not set, nothing happens. Else a new
     * clip is created.
     * 
     * @throws LineUnavailableException
     *             This error gets thrown when too many lines are in use.
     */
    public void play() throws LineUnavailableException {
        Clip clip;

        if (playMultipleTimes || clips.isEmpty()) {
            clip = generateClip();
        } else {
            clip = clips.get(0);
        }

        if (!clip.isOpen()) {
            clip.open(format, data, 0, data.length);
        }

        clip.start();
    }

    /**
     * Stop all clips.
     */
    public void stop() {
        for (Clip clip : clips) {
            clip.stop();
        }
    }

}
