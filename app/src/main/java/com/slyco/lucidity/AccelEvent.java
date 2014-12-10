package com.slyco.lucidity;

public class AccelEvent {
	public float mag;
	public long timestamp;
	public boolean expired;
	
	public AccelEvent(float m){
		mag = m;
		timestamp = Utilities.getTimeSinceEpoch();
		expired = false;
	}
	
	public boolean expire(long exp){
		if (Utilities.getTimeSinceEpoch() - timestamp >= exp)  {
			expired = true;
		}
		return expired;
	}
}
