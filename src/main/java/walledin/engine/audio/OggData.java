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

import java.nio.ByteBuffer;

/**
 * Data package read from Ogg file.
 * 
 * @author Ben Ruijl
 * 
 */
public class OggData {
    /** PCM data. */
    private ByteBuffer data;
    /** Rate of sound. */
    private final int rate;
    /** Channels. */
    private final int channels;

    public OggData(ByteBuffer data, int rate, int channels) {
        super();
        this.data = data;
        this.rate = rate;
        this.channels = channels;
    }

    public ByteBuffer getData() {
        return data;
    }

    public int getRate() {
        return rate;
    }

    public int getChannels() {
        return channels;
    }
}
