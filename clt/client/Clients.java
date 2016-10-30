package client;

import com.google.gson.JsonElement;


public class Clients implements Comparable {
	String id;		 // id extracted from the JASON description
	String name;
	String ciphers;
	JsonElement description; // JSON description of the client, including id
	
	Clients ( String id, String name, String ciphers, JsonElement description )
	{
		this.name = name;
	    this.id = id;
	    this.ciphers = ciphers;
	    this.description = description;
	}
	
	public int compareTo ( Object x )
	{
	    return ((Clients) x).id.compareTo ( id );
	}
	
	@Override
	public String toString(){
		return name;
	}
}
