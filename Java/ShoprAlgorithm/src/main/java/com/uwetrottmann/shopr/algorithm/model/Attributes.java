
package com.uwetrottmann.shopr.algorithm.model;

import com.uwetrottmann.shopr.algorithm.Query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Holds a list of possible attributes of an item.
 */
public class Attributes {

    public interface Attribute {
        public String id();

        public AttributeValue currentValue();

        public double[] getValueWeights();

        public String getValueWeightsString();

        public String getReasonString();

        public void critiqueQuery(Query query, boolean isPositive);
        
        public void updateQuery(Query query, Set<AttributeValue> preferredValues);
        
        public AttributeValue[] getAttributeValues();
        
        public AttributeValue getCurrentValue();
    }

    public interface AttributeValue {

        /**
         * Returns the index of this value in the weights vector of the
         * attribute.
         */
        public int index();

        /**
         * Returns a {@link String} representation suitable for end-user
         * representation.
         */
        public String descriptor();
        
        /**
         * Returns a {@link String} representation suitable for 
         * resource names.
         */
        public String simpleName();
        
        /**
         * Returns a {@link String} representation suitable for 
         * resource names.
         */
        public String explanatoryDescriptor();
        
        /**
         * Returns the color hex code for attribute visual representation.
         */
        public String color();
    }

    private HashMap<String, Attribute> attributes = new HashMap<String, Attributes.Attribute>();
    
    public Collection<Attribute> values() {
    	return attributes.values();
    }

    public Attribute getAttributeById(String id) {
        return attributes.get(id);
    }

    /**
     * Adds the new attribute mapped by its {@link Attribute#id()} or replaces
     * an existing one.
     */
    public Attributes putAttribute(Attribute attribute) {
        attributes.put(attribute.id(), attribute);
        return this;
    }

    public List<Attribute> getAllAttributes() {
        List<Attribute> attributeList = new ArrayList<Attribute>();
        attributeList.addAll(attributes.values());

        return attributeList;
    }

    public String getAllAttributesString() {
        StringBuilder attrsStr = new StringBuilder("");

        List<Attribute> allAttributes = getAllAttributes();
        for (int i = 0; i < allAttributes.size(); i++) {
            attrsStr.append(allAttributes.get(i).getValueWeightsString());
            if (i != allAttributes.size() - 1) {
                attrsStr.append(" ");
            }
        }

        return attrsStr.toString();
    }

    /**
     * Returns a string describing this query for an end-user, e.g.
     * "no Red, mainly Blue, no Armani".
     */
    public String getReasonString() {
        StringBuilder reason = new StringBuilder();

        List<Attribute> allAttributes = getAllAttributes();
        for (int i = 0; i < allAttributes.size(); i++) {
            Attribute attribute = allAttributes.get(i);
            String attrString = attribute.getReasonString();
            if (attrString != null && attrString.length() != 0) {
                if (reason.length() != 0) {
                    reason.append(", ");
                }
                reason.append(attrString);
            }
        }

        return reason.toString();
    }

    /**
     * Calls {@link #putAttribute(Attribute)} with a {@link GenericAttribute}
     * implementation matching the given id.
     */
    public Attribute initializeAttribute(Attribute attribute) {
    	Attribute newAttr = initialize(attribute);
    	if(newAttr != null) putAttribute(newAttr);
    	
    	return newAttr;
    }
    
    /**
     * Initialises a {@link GenericAttribute} implementation matching the given id.
     */
    public Attribute initialize(Attribute attribute) {
        try {
            Class<?> attrClass = Class.forName(attribute.getClass().getCanonicalName());
            Attribute newAttr = (Attribute) attrClass.newInstance();
            return newAttr;
        } catch (ClassNotFoundException ex) {
            System.err.println(ex + " Interpreter class must be in class path.");
        } catch (InstantiationException ex) {
            System.err.println(ex + " Interpreter class must be concrete.");
        } catch (IllegalAccessException ex) {
            System.err.println(ex + " Interpreter class must have a no-arg constructor.");
        }
		return null;
    }

}
