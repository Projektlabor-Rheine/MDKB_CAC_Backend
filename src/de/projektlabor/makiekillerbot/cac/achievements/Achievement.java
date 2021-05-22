package de.projektlabor.makiekillerbot.cac.achievements;

import java.io.IOException;

import com.google.zxing.WriterException;

import de.projektlabor.makiekillerbot.cac.util.QRCode;

public class Achievement {
	
	private String hintName;
	private String unlockId;
	private boolean hasBeenFound;
	
	// Id of the achievement. This is not constant. It can change on different loads
	private int loadId;
	
	// The qr-code that is corresponding to this achievement
	private QRCode qrCode;
	
	public Achievement(int loadId,String hintName,String unlockId,QRCode code,boolean hasBeenFound) throws WriterException, IOException {
		this.loadId=loadId;
		this.hintName=hintName;
		this.unlockId=unlockId;
		this.hasBeenFound=hasBeenFound;
		this.qrCode = code;
	}

	public String getHintName() {
		return this.hintName;
	}
	public String getUnlockId() {
		return this.unlockId;
	}
	public boolean hasBeenFound() {
		return this.hasBeenFound;
	}
	public void setHasBeenFound(boolean hasBeenFound) {
		this.hasBeenFound = hasBeenFound;
	}
	public int getLoadId() {
		return this.loadId;
	}
	public QRCode getQRCode() {
		return this.qrCode;
	}
}
