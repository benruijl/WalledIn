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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import net.java.games.joal.AL;
import net.java.games.joal.ALC;
import net.java.games.joal.ALCcontext;
import net.java.games.joal.ALCdevice;
import net.java.games.joal.ALException;
import net.java.games.joal.ALFactory;
import net.java.games.sound3d.AudioSystem3D;

import org.apache.log4j.Logger;

import walledin.engine.math.Vector2f;

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

    /**
     * Private constructor.
     */
    private Audio() {
        samples = new HashMap<String, Integer>();

        initializeSystem();
        initializeListener();
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
     * Initializes the OpenAL system.
     */
    private void initializeSystem() {
        AudioSystem3D.init();

        alc = ALFactory.getALC();
        al = ALFactory.getAL();

        ALCdevice device;
        ALCcontext context;
        String deviceID;

        // Get handle to default device.
        device = alc.alcOpenDevice(null);
        if (device == null) {
            LOG.error("Error opening default OpenAL device", new ALException());
        }

        deviceID = alc.alcGetString(device, ALC.ALC_DEVICE_SPECIFIER);
        if (deviceID == null) {
            LOG.error("Error getting specifier for default OpenAL device",
                    new ALException());
        }

        LOG.info("Using sound device " + deviceID);

        // Create audio context.
        context = alc.alcCreateContext(device, null);
        if (context == null) {
            LOG.error("Can't create OpenAL context", new ALException());
        }
        alc.alcMakeContextCurrent(context);

        if (alc.alcGetError(device) != ALC.ALC_NO_ERROR) {
            LOG.error("Unable to make context current", new ALException());
        }
    }

    /**
     * Initializes the listener.
     */
    public void initializeListener() {
        float[] listenerPos = { 0.0f, 0.0f, 0.0f };
        float[] listenerVel = { 0.0f, 0.0f, 0.0f };
        float[] listenerOri = { 0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f };
        al.alListenerfv(AL.AL_POSITION, listenerPos, 0);
        al.alListenerfv(AL.AL_VELOCITY, listenerVel, 0);
        al.alListenerfv(AL.AL_ORIENTATION, listenerOri, 0);
    }

    /**
     * Sets the position of the listener. Usually the position of the player.
     * 
     * @param position
     *            Listener position.
     */
    public void setListenerPosition(final Vector2f position) {
        al.alListener3f(AL.AL_POSITION, position.getX(), position.getX(), 0);
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
            AudioFormat audioFormat = new AudioFormat(stream.getFormat()
                    .getSampleRate(), 16, stream.getFormat().getChannels(),
                    true, false);

            stream = AudioSystem.getAudioInputStream(audioFormat, stream);

            /* Read the stream. */
            byte[] data = new byte[(int) stream.getFrameLength()
                    * audioFormat.getFrameSize()];
            stream.read(data);

            IntBuffer buffer = IntBuffer.allocate(1);
            al.alGenBuffers(1, buffer);
            al.alBufferData(
                    buffer.get(0),
                    stream.getFormat().getChannels() > 1 ? AL.AL_FORMAT_STEREO16
                            : AL.AL_FORMAT_MONO16, ByteBuffer.wrap(data),
                    data.length, (int) audioFormat.getSampleRate());

            samples.put(name, buffer.get(0));

        } catch (Exception e) {
            e.printStackTrace();
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
            OggDecoder decoder = new OggDecoder();
            OggData ogg = decoder.getData(url.openStream());

            IntBuffer buffer = IntBuffer.allocate(1);
            al.alGenBuffers(1, buffer);
            al.alBufferData(buffer.get(0),
                    ogg.getChannels() > 1 ? AL.AL_FORMAT_STEREO16
                            : AL.AL_FORMAT_MONO16, ByteBuffer.wrap(ogg
                            .getData()), ogg.getData().length, ogg.getRate());

            samples.put(name, buffer.get(0));

        } catch (IOException e) {
            LOG.info("Could not load file: " + url.getFile());
            e.printStackTrace();
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

            IntBuffer sources = IntBuffer.allocate(1);
            al.alGenSources(1, sources);
            int source = sources.get(0);

            al.alSourcei(source, AL.AL_BUFFER, samples.get(name));
            al.alSourcef(source, AL.AL_PITCH, 1.0f);
            /* Full volume. */
            al.alSourcef(source, AL.AL_GAIN, 1.0f);
            al.alSource3f(source, AL.AL_POSITION, sourcePosition.getX(),
                    sourcePosition.getY(), 0);
            al.alSource3f(source, AL.AL_VELOCITY, 0, 0, 0);
            al.alSourcei(source, AL.AL_LOOPING, loop ? AL.AL_TRUE : AL.AL_FALSE);

            /* Play the sound */
            al.alSourcePlay(sources.get(0));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
