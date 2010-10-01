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
package walledin.engine.physics;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jbox2d.dynamics.ContactListener;
import org.jbox2d.dynamics.contacts.ContactPoint;
import org.jbox2d.dynamics.contacts.ContactResult;

public class GeneralContactListener implements
        org.jbox2d.dynamics.ContactListener {
    private static final Logger LOG = Logger
            .getLogger(GeneralContactListener.class);

    Map<Object, ContactListener> listeners;

    public GeneralContactListener() {
        listeners = new HashMap<Object, ContactListener>();
    }

    public void addListener(Object object, ContactListener listener) {
        listeners.put(object, listener);
    }

    public void removeListener(ContactListener listener) {
        listeners.values().remove(listener);
    }

    @Override
    public void add(ContactPoint point) {
        ContactListener listenerA = listeners.get(point.shape1.m_body.getUserData());
        ContactListener listenerB = listeners.get(point.shape2.m_body.getUserData());
        
        if (listenerA != null) {
            listenerA.add(point);
        }

        if (listenerB != null) {
            listenerB.add(point);
        }
    }

    @Override
    public void persist(ContactPoint point) {
        ContactListener listenerA = listeners.get(point.shape1.m_body.getUserData());
        ContactListener listenerB = listeners.get(point.shape2.m_body.getUserData());
        if (listenerA != null) {
            listenerA.persist(point);
        }

        if (listenerB != null) {
            listenerB.persist(point);
        }
    }

    @Override
    public void remove(ContactPoint point) {
        ContactListener listenerA = listeners.get(point.shape1.m_body.getUserData());
        ContactListener listenerB = listeners.get(point.shape2.m_body.getUserData());
        if (listenerA != null) {
            listenerA.remove(point);
        }

        if (listenerB != null) {
            listenerB.remove(point);
        }

    }

    @Override
    public void result(ContactResult point) {
        ContactListener listenerA = listeners.get(point.shape1.m_body.getUserData());
        ContactListener listenerB = listeners.get(point.shape2.m_body.getUserData());
        if (listenerA != null) {
            listenerA.result(point);
        }

        if (listenerB != null) {
            listenerB.result(point);
        }

    }

}
