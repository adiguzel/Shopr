package com.uwetrottmann.shopr.algorithm.model;

import java.util.Arrays;

import com.uwetrottmann.shopr.algorithm.model.Attributes.AttributeValue;

public class Sex extends GenericAttribute {

	public static final String ID = "sex";

	public enum Value implements AttributeValue {
		FEMALE("Female", Colors.PINK), 
		MALE("Male", Colors.BLUE), 
		UNISEX("Unisex", Colors.GREEN);

		private String mDescriptor;
		private String mColor;

		Value(String name, String color) {
			mDescriptor = name;
			mColor = color;
		}

		@Override
		public String descriptor() {
			return mDescriptor;
		}

		@Override
		public int index() {
			return ordinal();
		}

		@Override
		public String color() {
			return mColor;
		}
	}

	public Sex() {
		int numValues = Value.values().length;
		mValueWeights = new double[numValues];
		Arrays.fill(mValueWeights, 1.0 / numValues);
	}

	public Sex(Value value) {
		setWeights(value);
	}

	/**
	 * Tries to match the given string with a {@link Sex.Value}.
	 */
	public Sex(String value) {
		if ("Weiblich".equals(value)) {
			setWeights(Sex.Value.FEMALE);
		} else if ("Männlich".equals(value) || "M�nnlich".equals(value)) {
			setWeights(Sex.Value.MALE);
		} else if ("Beide".equals(value)) {
			setWeights(Sex.Value.UNISEX);
		}
	}

	private void setWeights(Value value) {
		mValueWeights = new double[Value.values().length];
		Arrays.fill(mValueWeights, 0.0);
		mValueWeights[value.ordinal()] = 1.0;
		currentValue(value);
	}

	@Override
	public String id() {
		return ID;
	}

	@Override
	public AttributeValue[] getValueSymbols() {
		return Value.values();
	}

	@Override
	public AttributeValue[] getAttributeValues() {
		return Value.values();
	}
}
