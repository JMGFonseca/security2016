package client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;


public class Clients implements Comparable {
	String id;		 // id extracted from the JASON description
	JsonElement description; // JSON description of the client, including id
	
	Clients ( String id, JsonElement description )
	{
	    this.id = id;
	    this.description = description;
	}
	
	public int compareTo ( Object x )
	{
	    return ((Clients) x).id.compareTo ( id );
	}
	
	@Override
	public String toString(){
		JsonObject j = description.getAsJsonObject();
		return j.get("name").toString().replaceAll("\"", "");
	}
}
