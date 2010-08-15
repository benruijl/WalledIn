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
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import net.java.games.joal.AL;
import net.java.games.joal.ALC;
import net.java.games.joal.ALCConstants;
import net.java.games.joal.ALCcontext;
import net.java.games.joal.ALCdevice;
import net.java.games.joal.ALConstants;
import net.java.games.joal.ALException;
import net.java.games.joal.ALFactory;
import net.java.games.sound3d.AudioSystem3D;

import org.apache.log4j.Logger;

import walledin.engine.math.Vector2f;
import walledin.util.SettingsManager;

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

    /** JOAL instance. */
    private AL al;
    /** JOAL context manager. */
    private ALC alc;
    /** Map of sample name to sample. */
    private final Map<String, Integer> samples;
    /** Buffer of OpenAL sources. */
    private final List<Integer> sources;
    /** Is sound enabled? */
    private boolean enabled;

    /**
     * Private constructor.
     */
    private Audio() {
        samples = new HashMap<String, Integer>();
        sources = new LinkedList<Integer>();
        
        if (!SettingsManager.getInstance().getBoolean("game.audio")) {
            enabled = false;
            return;
        }

        enabled = false;
        initializeSystem();

        if (enabled) {
            initializeListener();
        }
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
     * Is the sound enabled?
     * 
     * @return True if enabled, else false
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Initializes the OpenAL system.
     */
    private void initializeSystem() {
        AudioSystem3D.init();

        alc = ALFactory.getALC();
        al = ALFactory.getAL();

        ALCdevice device;
        ALCcontext context;
        String deviceID;

        try {
            // Get handle to default device.
            device = alc.alcOpenDevice(null);
            if (device == null) {
                LOG.error("Error opening default OpenAL device",
                        new ALException());
                return;
            }

            deviceID = alc.alcGetString(device,
                    ALCConstants.ALC_DEVICE_SPECIFIER);
            if (deviceID == null) {
                LOG.error("Error getting specifier for default OpenAL device",
                        new ALException());
                return;
            }

            LOG.info("Using sound device " + deviceID);

            // Create audio context.
            context = alc.alcCreateContext(device, null);
            if (context == null) {
                LOG.error("Can't create OpenAL context", new ALException());
                return;
            }
            alc.alcMakeContextCurrent(context);

            if (alc.alcGetError(device) != ALCConstants.ALC_NO_ERROR) {
                LOG.error("Unable to make context current", new ALException());
                return;
            }

        } catch (final Exception e) {
            LOG.error("Exception during initialization "
                    + "of audio system. Disabling audio.", e);
            return;
        }

        enabled = true;
    }

    /**
     * Initializes the listener.
     */
    public void initializeListener() {
        final float[] listenerPos = { 0.0f, 0.0f, 0.0f };
        final float[] listenerVel = { 0.0f, 0.0f, 0.0f };
        final float[] listenerOri = { 0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f };
        al.alListenerfv(ALConstants.AL_POSITION, listenerPos, 0);
        al.alListenerfv(ALConstants.AL_VELOCITY, listenerVel, 0);
        al.alListenerfv(ALConstants.AL_ORIENTATION, listenerOri, 0);
    }

    /**
     * Sets the position of the listener. Usually the position of the player.
     * 
     * @param position
     *            Listener position.
     */
    public void setListenerPosition(final Vector2f position) {
        al.alListener3f(ALConstants.AL_POSITION, position.getX(),
                position.getX(), 0);
    }

    /**
     * Loads a Wave sample.
     * 
     * @param name
     *            Name of resource
     * @param url
     *            URL of resource
     */
    public void loadWaveSample(final String name, final URL url) {
        AudioInputStream stream;
        try {
            stream = AudioSystem.getAudioInputStream(url);

            /* Convert audio format. */
            final AudioFormat audioFormat = new AudioFormat(stream.getFormat()
                    .getSampleRate(), 16, stream.getFormat().getChannels(),
                    true, false);

            stream = AudioSystem.getAudioInputStream(audioFormat, stream);

            /* Read the stream. */
            final byte[] data = new byte[(int) stream.getFrameLength()
                    * audioFormat.getFrameSize()];
            stream.read(data);

            final IntBuffer buffer = IntBuffer.allocate(1);
            al.alGenBuffers(1, buffer);
            final int format = stream.getFormat().getChannels() > 1 ? ALConstants.AL_FORMAT_STEREO16
                    : ALConstants.AL_FORMAT_MONO16;
            al.alBufferData(buffer.get(0), format, ByteBuffer.wrap(data),
                    data.length, (int) audioFormat.getSampleRate());

            samples.put(name, buffer.get(0));

        } catch (final Exception e) {
            LOG.info("Could not load file: " + url.getFile(), e);
        }
    }

    /**
     * Loads an Ogg sample.
     * 
     * @param name
     *            Name of the sample
     * @param url
     *            URL of the sample
     */
    public void loadOggSample(final String name, final URL url) {
        try {
            final OggDecoder decoder = new OggDecoder();
            final OggData ogg = decoder.getData(url.openStream());

            final IntBuffer buffer = IntBuffer.allocate(1);
            al.alGenBuffers(1, buffer);
            al.alBufferData(buffer.get(0),
                    ogg.getChannels() > 1 ? ALConstants.AL_FORMAT_STEREO16
                            : ALConstants.AL_FORMAT_MONO16, ByteBuffer.wrap(ogg
                            .getData()), ogg.getData().length, ogg.getRate());

            samples.put(name, buffer.get(0));

        } catch (final IOException e) {
            LOG.info("Could not load file: " + url.getFile(), e);
        }
    }

    /**
     * Plays a sample.
     * 
     * @param name
     *            Sample name
     * @param sourcePosition
     *            The position of the audio source
     * @param loop
     *            True is sound should loop, else false
     */
    public void playSample(final String name, final Vector2f sourcePosition,
            final boolean loop) {
        try {
            if (!samples.containsKey(name)) {
                LOG.warn("Audio file + " + name + " not found.");
                return;
            }

            final IntBuffer sourceBuffer = IntBuffer.allocate(1);
            al.alGenSources(1, sourceBuffer);
            final int source = sourceBuffer.get(0);
            sources.add(source);

            al.alSourcei(source, ALConstants.AL_BUFFER, samples.get(name));
            al.alSourcef(source, ALConstants.AL_PITCH, 1.0f);
            /* Full volume. */
            al.alSourcef(source, ALConstants.AL_GAIN, 1.0f);
            al.alSource3f(source, ALConstants.AL_POSITION,
                    sourcePosition.getX(), sourcePosition.getY(), 0);
            al.alSource3f(source, ALConstants.AL_VELOCITY, 0, 0, 0);
            al.alSourcei(source, ALConstants.AL_LOOPING,
                    loop ? ALConstants.AL_TRUE : ALConstants.AL_FALSE);

            /* Play the sound */
            al.alSourcePlay(source);

        } catch (final Exception e) {
            LOG.info("Could not play sample: " + name, e);
        }
    }

    /**
     * If enabled, updates the audio component.
     */
    public void update() {
        if (enabled) {
            removeUnusedSources();
        }
    }

    /**
     * Checks the sources list for inactive sources and removes them.
     */
    private void removeUnusedSources() {
        if (al != null) {
            final int[] state = new int[1];

            final ListIterator<Integer> it = sources.listIterator();
            while (it.hasNext()) {
                final int source = it.next();
                al.alGetSourcei(source, ALConstants.AL_SOURCE_STATE, state, 0);

                if (state[0] == ALConstants.AL_STOPPED) {
                    final int[] so = new int[] { source };
                    al.alDeleteSources(1, so, 0);
                    it.remove();
                }
            }
        }
    }

    /**
     * Removes the buffers, the sources and the OpenAL context.
     */
    public void cleanUp() {
        for (final Integer source : sources) {
            final int[] so = new int[] { source };
            al.alDeleteSources(1, so, 0);
        }

        for (final Integer buffer : samples.values()) {
            final int[] buf = new int[] { buffer };
            al.alDeleteBuffers(1, buf, 0);
        }

        ALCcontext curContext;
        ALCdevice curDevice;

        curContext = alc.alcGetCurrentContext();
        curDevice = alc.alcGetContextsDevice(curContext);
        alc.alcMakeContextCurrent(null);
        alc.alcDestroyContext(curContext);
        alc.alcCloseDevice(curDevice);

        al = null;
        alc = null;
    }

}
