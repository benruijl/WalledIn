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
package walledin.game.entity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import walledin.game.EntityManager;

public class Entity {
    private final static Logger LOG = Logger.getLogger(Entity.class.getName());
    private final Map<Class<? extends Behavior>, Behavior> behaviors;
    private final Map<Attribute, Object> attributes;
    private Set<Attribute> changedAttributes;
    private String name;
    private final Family family;
    private boolean markedRemoved;
    private final EntityManager entityManager;

    /**
     * 
     * @param name
     *            Name of the component
     */
    public Entity(final EntityManager entityManager, final Family family,
            final String name) {
        behaviors = new HashMap<Class<? extends Behavior>, Behavior>();
        attributes = new HashMap<Attribute, Object>();
        changedAttributes = new HashSet<Attribute>();
        this.name = name;
        this.family = family;
        markedRemoved = false;
        this.entityManager = entityManager;
    }

    /**
     * Get the name of the entity
     * 
     * @return Name of the entity
     */
    public String getName() {
        return name;
    }

    /**
     * Get the family
     * 
     * @return Family
     */
    public Family getFamily() {
        return family;
    }

    /**
     * Set the name of the entity
     * 
     * @return Name of the entity
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Adds a behavior to this class
     * 
     * @param behavior
     *            The behavior instance to be added
     * @throws IllegalArgumentException
     *             Thrown when the entity already contains an behavior of this
     *             type
     */
    public void addBehavior(final Behavior behavior) {
        final Class<? extends Behavior> clazz = behavior.getClass();
        if (behaviors.containsKey(clazz)) {
            throw new IllegalArgumentException("Entity [" + toString()
                    + "] already contains Component of class: "
                    + behavior.getClass().getName());
        }
        behaviors.put(clazz, behavior);
    }

    /**
     * Retrieves a behavior of this entity based on the class of the behavior
     * 
     * @param <T>
     *            The type of the Behavior that has to be fetched
     * @param clazz
     *            Class of the Behavior that has to be fetched
     * @return The requested behavior or null if this entity doesn't have a
     *         behavior of this class
     */
    @SuppressWarnings("unchecked")
    public <T extends Behavior> T getBehavior(final Class<T> clazz) {
        if (!behaviors.containsKey(clazz)) {
            throw new IllegalArgumentException("Object " + name
                    + " does not have behaviour of class " + clazz.getName());
        }

        final T beh = (T) behaviors.get(clazz);
        return beh;
    }

    /**
     * Checks if entity has a certain attribute. Useful in situations where you
     * want to filter some entities.
     * 
     * @param attribute
     *            The attribute to check
     * @return True if entity has a non-null attribute, else false
     */
    // TODO: remove
    @Deprecated
    public boolean hasAttribute(final Attribute attribute) {
        return attributes.containsKey(attribute)
                && attributes.get(attribute) != null;
    }

    /**
     * Gets the object bound to the attribute.
     * 
     * @param attribute
     *            The attribute to get
     * @return Returns the object bound to this attribute
     */
    public Object getAttribute(final Attribute attribute) {
        if (!attributes.containsKey(attribute)) {
            LOG.warning("Object " + name + " does not have attribute "
                    + attribute.name());
        }

        return attributes.get(attribute);
    }

    /**
     * Binds the object to the attribute and returns the old object
     * 
     * @param attribute
     *            The attribute to bind
     * @param newObject
     *            Object to bind
     * @return Returns the object bound to the attribute before
     * @throws IllegalArgumentException
     *             if the newObect is not of the class specified in the
     *             attribute
     */
    @SuppressWarnings("unchecked")
    public <T> T setAttribute(final Attribute attribute, final T newObject) {
        if (attribute.clazz.isInstance(newObject) || newObject == null) {
            final T result = (T) attributes.put(attribute, newObject);
            // Only add it if it is actually changed
            if (attribute.sendOverNetwork && !newObject.equals(result)) {
                changedAttributes.add(attribute);
            }
            sendMessage(MessageType.ATTRIBUTE_SET, attribute);
            return result;
        } else {
            throw new IllegalArgumentException("Object should be of class: "
                    + attribute.clazz.getName());
        }
    }

    /**
     * Get attributes that can be send over the network that have been changed
     * since the last call
     * 
     * @return The changed attributes
     */
    public Set<Attribute> getChangedAttributes() {
        Set<Attribute> temp = changedAttributes;
        changedAttributes = new HashSet<Attribute>();
        return temp;
    }

    public Map<Attribute, Object> getAttributes(
            Set<Attribute> requestedAttributes) {
        final Map<Attribute, Object> temp = new HashMap<Attribute, Object>();
        for (final Attribute attribute : requestedAttributes) {
            temp.put(attribute, attributes.get(attribute));
        }
        return temp;
    }

    /**
     * Removes a behavior from this entity
     * 
     * @param clazz
     *            Class of the behavior to be removed
     * @return The behavior that was removed
     */
    public Behavior removeBehavior(final Class<? extends Behavior> clazz) {
        final Behavior behavior = behaviors.remove(clazz);
        return behavior;
    }

    /**
     * Calls onMessage on all the behaviors of this entity
     */
    public void sendMessage(final MessageType messageType, final Object data) {
        for (final Behavior behavior : behaviors.values()) {
            behavior.onMessage(messageType, data);
        }
    }

    /**
     * Calls onUpdate on all the behaviors of this entity
     */
    public void sendUpdate(final double delta) {
        for (final Behavior behavior : behaviors.values()) {
            behavior.onUpdate(delta);
        }
    }

    /**
     * Mark this entity for removal
     */
    public void remove() {
        markedRemoved = true;
    }

    public boolean isMarkedRemoved() {
        return markedRemoved;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }
}
