package com.woodlawn.globaldominion.game;

public class Units {

	public static final int TYPE_INFANTRY = 1;
	
	private static final float DAMAGE_INFANTRY = 5f;
	private static final float ARMOR_INFANTRY = 5f;
	private static final int PRIORITY_INFANTRY = 1;
	
	private float mDamage;
	private float mArmor;
	private int mPriority;
	private int mType;
	
	public Units(int type) {
		mType = type;
		switch(type) {
		case TYPE_INFANTRY:
			mDamage = DAMAGE_INFANTRY;
			mArmor = ARMOR_INFANTRY;
			mPriority = PRIORITY_INFANTRY;
			break;
		}
	}	
	
	public float attack(float factor) {
		return factor * mDamage;
	}
	
	public float defend(float factor) {
		return mArmor * factor;
	}
	
	public int getPriority() {
		return mPriority;
	}
	
	public float defeated(float attackValue) {
		mArmor = mArmor - attackValue;
		return attackValue - mArmor;
	}
	
	public int getType() {
		return mType;
	}
	
	public float armor() {
		return mArmor;
	}
}
