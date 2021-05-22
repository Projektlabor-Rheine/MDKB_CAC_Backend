package de.projektlabor.makiekillerbot.cac.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

public class QRCode {

	// The generated qr-code image (stored as png)
	private byte[] imageData;
	
	// The string-content of the qr-code
	private String link;
	
	private QRCode(byte[] imageData,String content) {
		this.link = content;
		this.imageData = imageData;
	}
	
	/**
	 * Creates a qr-code from the given data and returns it as a byte[] encoded using the png-format
	 * @param data the data that shall be stored
	 * @param width dimension of the image on the x axis
	 * @param height dimension of the image on the y axis
	 * @return the byte[] with the stored image (encoded in png-format)
	 * @throws WriterException if a problem occurred while generating the qr-code
	 * @throws IOException if a problem occurred while converting the code to an image
	 */
	public static QRCode createQRCodeBytesFromString(String data,int width,int height) throws WriterException, IOException {
		// Creates the qr-code
		BitMatrix bm = new MultiFormatWriter().encode(data, BarcodeFormat.QR_CODE, width, height);
		
		// Converts the code to an image
		BufferedImage im = MatrixToImageWriter.toBufferedImage(bm);
		
		// Writes the code to a byte[] using png-format
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ImageIO.write(im, "png", os);
		
		return new QRCode(os.toByteArray(), data);
	}
	
	public String getRawContent() {
		return this.link;
	}
	
	public byte[] getImageData() {
		return this.imageData;
	}
	
}
