/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.model;

import com.apptentive.android.sdk.Log;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Sky Kelsey
 */
public class RecordFactory {

	/**
	 * Constructing a JSONObject is zero cost, since nothing is parsed. So this implementation allows creating a JSONObject
	 * multiple times and should be efficient.
	 *
	 * @param json
	 * @return
	 */
	public static ActivityFeedItem fromJson(String json) {
		try {
			JSONObject root = new JSONObject(json);
			ActivityFeedItem.BaseType type = ActivityFeedItem.BaseType.parse(root.getString(ActivityFeedItem.KEY_TYPE));
			return fromJson(json, type);
		} catch (JSONException e) {
			// This is a low level log for forward compatibility.
			Log.d("Exception parsing json as Record: %s", e, json);
		}
		return null;
	}

	public static ActivityFeedItem fromJson(String json, ActivityFeedItem.BaseType baseType) {
		switch (baseType) {
			case message:
				return MessageFactory.fromJson(json);
			case event:
				return EventFactory.fromJson(json);
			case unknown:
				Log.v("Ignoring unknown RecordType.");
				break;
			default:
				break;
		}
		return null;
	}
}
