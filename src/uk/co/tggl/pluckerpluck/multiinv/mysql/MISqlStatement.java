package uk.co.tggl.pluckerpluck.multiinv.mysql;

import org.bukkit.OfflinePlayer;

import uk.co.tggl.pluckerpluck.multiinv.books.MIBook;

public class MISqlStatement {

	boolean checkplayerinvcreated = false;
	boolean checkchestinvcreated = false;
	String statement = "";
	OfflinePlayer player = null;
	String group = "";
	String inventoryColumn = null;
	MIBook book = null;
	
	public MISqlStatement(String statement) {
		this.statement = statement;
	}
	
	public MISqlStatement(MIBook book) {
		this.book = book;
	}
	
	public MISqlStatement(String statement, OfflinePlayer player, String group) {
		this.statement = statement;
		this.player = player;
		this.group = group;
	}
	
	public void setCheckPlayerInv(boolean check) {
		checkplayerinvcreated = check;
	}
	
	public boolean checkPlayerInv() {
		return checkplayerinvcreated;
	}
	
	public void setCheckChestInv(boolean check) {
		checkchestinvcreated = check;
	}
	
	public boolean checkChestInv() {
		return checkchestinvcreated;
	}

	public OfflinePlayer getPlayer() {
		return player;
	}

	public String getGroup() {
		return group;
	}
	
	public String getStatement() {
		return statement;
	}

	public String getInventoryColumn() {
		return inventoryColumn;
	}

	public void setInventoryColumn(String inventoryColumn) {
		this.inventoryColumn = inventoryColumn;
	}

	public MIBook getBook() {
		return book;
	}

	public void setBook(MIBook book) {
		this.book = book;
	}
	
}
