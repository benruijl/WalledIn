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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jbox2d.dynamics.ContactListener;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.dynamics.contacts.ContactPoint;
import org.jbox2d.dynamics.contacts.ContactResult;

public class GeneralContactListener implements ContactListener {
    private static final Logger LOG = Logger
            .getLogger(GeneralContactListener.class);

    private List<ContactPoint> contactsAdded;
    private List<ContactPoint> contactsRemoved;
    private List<ContactPoint> contactsPersist;
    private List<ContactResult> contactsResult;
    private List<ContactListener> listeners;

    public GeneralContactListener() {
        contactsAdded = new ArrayList<ContactPoint>();
        contactsRemoved = new ArrayList<ContactPoint>();
        contactsPersist = new ArrayList<ContactPoint>();
        contactsResult = new ArrayList<ContactResult>();

        listeners = new ArrayList<ContactListener>();
    }

    public List<ContactListener> getListeners() {
        return listeners;
    }

    public void addListener(ContactListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ContactListener listener) {
        listeners.remove(listener);
    }

    /**
     * Copies a contact point. The point variable is changed by box2d.
     * 
     * @param point
     *            Point
     * @return Copy of point
     */
    private ContactPoint copyContactPoint(ContactPoint point) {
        ContactPoint newPoint = new ContactPoint();
        newPoint.friction = point.friction;
        newPoint.id = point.id;
        newPoint.normal = point.normal;
        newPoint.position = point.position;
        newPoint.restitution = point.restitution;
        newPoint.separation = point.separation;
        newPoint.shape1 = point.shape1;
        newPoint.shape2 = point.shape2;
        newPoint.velocity = point.velocity;

        return newPoint;
    }

    /**
     * Copies a contact result point. The point variable is changed by box2d.
     * 
     * @param point
     *            Point
     * @return Copy of point
     */
    private ContactResult copyContactResultPoint(ContactResult point) {
        ContactResult newPoint = new ContactResult();
        newPoint.id = point.id;
        newPoint.normal = point.normal;
        newPoint.normalImpulse = point.normalImpulse;
        newPoint.tangentImpulse = point.tangentImpulse;
        newPoint.position = point.position;
        newPoint.shape1 = point.shape1;
        newPoint.shape2 = point.shape2;

        return newPoint;
    }

    @Override
    public void add(ContactPoint point) {
        contactsAdded.add(copyContactPoint(point));
    }

    /**
     * Sends contact events to the listeners. Bodies cannot be created in
     * callback events. Therefore, a part of the contact processing is done
     * here.
     */
    public void update() {
        for (ContactPoint point : contactsAdded) {
            for (ContactListener listener : listeners) {
                listener.add(point);
            }
        }

        for (ContactPoint point : contactsPersist) {
            for (ContactListener listener : listeners) {
                listener.persist(point);
            }
        }

        for (ContactPoint point : contactsRemoved) {
            for (ContactListener listener : listeners) {
                listener.remove(point);
            }
        }

        for (ContactResult point : contactsResult) {
            for (ContactListener listener : listeners) {
                listener.result(point);
            }
        }

        contactsAdded.clear();
        contactsPersist.clear();
        contactsRemoved.clear();
        contactsResult.clear();
    }

    @Override
    public void persist(ContactPoint point) {
        contactsPersist.add(copyContactPoint(point));
    }

    @Override
    public void remove(ContactPoint point) {
        contactsRemoved.add(copyContactPoint(point));
    }

    @Override
    public void result(ContactResult point) {
        contactsResult.add(copyContactResultPoint(point));
    }

}
