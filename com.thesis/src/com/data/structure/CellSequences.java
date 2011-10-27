package com.data.structure;

import java.io.Serializable;

import com.interfaces.Sequences;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;


@DatabaseTable
public abstract class CellSequences implements Serializable, Sequences 
{

	private static final long serialVersionUID = -2181615333013945029L;
  
	@DatabaseField(generatedId = true)
	private int id;
	
	@DatabaseField
	private String startCellPoint;
	
	@DatabaseField
	private String endCellPoint;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getStartPoint() {
		return startCellPoint;
	}

	public void setStartPoint(String startPoint) {
		this.startCellPoint = startPoint;
	}

	public String getEndPoint() {
		return endCellPoint;
	}

	public void setEndPoint(String endPoint) {
		this.endCellPoint = endPoint;
	}
}
