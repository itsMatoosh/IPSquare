package me.matoosh.ipsquare;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet6Address;
import java.net.UnknownHostException;

import javax.imageio.ImageIO;

/**
 * Used to convert IPv6 addresses to square images.
 * @author Mateusz Rêbacz
 *
 */
public class IPConverter {
	
	public static void main(String[] args) {
		System.out.println("Welcome to IPSquare! :)");
		System.out.println(" Type 1 for address to image conversion.");
		System.out.println(" Type 2 for image to address conversion.");
		
		//Reading the console input.
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		int answer = 0;
		try {
			answer = Integer.parseInt(br.readLine());
		} catch (IOException e) {
			System.out.println("There was a problem with reading your input, try again...");
			e.printStackTrace();
			main(null);
			return;
		}
		
		System.out.println();
		
		if(answer == 1) {
			System.out.println("Specify the IPv6 address to convert...");
			System.out.println("Address: ");
			
			//Reading the console input.
			String addressWord = null;
			try {
				addressWord = br.readLine();
			} catch (IOException e) {
				System.out.println("There was a problem with reading your input, try again...");
				e.printStackTrace();
				main(null);
				return;
			}
			
			//Parsing the address.
			Inet6Address address = null;
			try {
				address = (Inet6Address) Inet6Address.getByName(addressWord);
			} catch (UnknownHostException e) {
				System.out.println("Your address is not in a correct format, try again...");
				System.out.println(addressWord);
				e.printStackTrace();
				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {}
				main(null);
				return;
			}
			
			if(!saveResult(address.toString(), intArrayToImg(addressToIntArray(address)))) {
				System.out.println("There was a problem generating the image!");
			} else {
				System.out.println("Image saved at: " + "./results/" + (address.toString().replaceAll("/", "").replaceAll(":", "-")));
			}
		} else if(answer == 2) {
			System.out.println("Specify the path of the image you want to convert...");
			System.out.println("Path: ");
			
			//Reading the console input.
			String pathWord = null;
			try {
				pathWord = br.readLine();
			} catch (IOException e) {
				System.out.println("There was a problem with reading your input, try again...");
				e.printStackTrace();
				main(null);
				return;
			}
			
			//Parsing the path.
			File path = new File(pathWord);
			if(!path.exists() || !path.isFile())  {
				System.out.println("The path you specified does not exist, try again!");
				main(null);
				return;
			}
			if(!path.toString().contains(".png")) {
				System.out.println("The file you specified is not a png file, try again!");
				main(null);
				return;
			}
			
			BufferedImage img = null;
			try {
			    img = ImageIO.read(path);
			} catch (IOException e) {
				System.out.println("There was a problem reading the file, try again!");
				main(null);
				return;
			}
			if(img.getHeight() != 6 || img.getWidth() != 6) {
				img = createResizedCopy(img, 6, 6);
			}
			
			System.out.println("Address of the image: " + intArrayToAddress(imgToIntArray(img)));
		}
	}

	
	/**
	 * Converts an address to int array.
	 * @param address
	 */
	public static int[][] addressToIntArray(Inet6Address address) {
		int[][] result = new int[6][6];
		String addressString = address.getHostAddress();
		if(addressString.startsWith("/")) {
			addressString = addressString.substring(1, addressString.length());
		}
		addressString = expandAddress(addressString);
		
		//System.out.println(addressString);
		
		for(int r = 0; r < 6; r++) {
			if(addressString.length() < r*6) break;
			if(addressString.substring(r*6).length() < 6) {
				//Last row
				//System.out.println(addressString.substring(r*6));
				
				for(int c = 0; c < 6; c++) {
					//System.out.println(sub.toCharArray()[g]);
					if(c > 1) {
						result[r][c] = 0;
					} else {
						result[r][c] = addressString.substring(r*6).toCharArray()[c];	
					}
				}
			} else {
				String sub = (String) addressString.subSequence(r*6, r*6 + 6);
				for(int c = 0; c < sub.length(); c++) {
					//System.out.println();
					//System.out.println(sub.toCharArray()[c]);
					result[r][c] = (char)sub.toCharArray()[c];
				}
			}
		}
		
		for(int i = 0; i <= 5; i++) {
			System.out.println();
			for(int y = 0; y <= 5; y++) {
				if(result[i][y] == 0) {
					System.out.print("- ");
				} else {
					System.out.print((char)result[i][y] + " ");
				}
			}
		}
		System.out.println();
		
		return result;
	}
	
	/**
	 * Expands the given IPv6 address.
	 * @param address
	 * @return
	 */
	private static String expandAddress(String address) {
		String[] chunks = address.split(":");
		String result = "";
		
		for(String chunk : chunks) {
			if(chunk.equals("")) {
				result += "0000";
			} else if(chunk.length() < 4) {
				String zeros = "";
				for(int i = 4 - chunk.length(); i > 0; --i) {
					zeros += "0";
				}
				result += zeros + chunk;
			}
			else {
				result += chunk;	
			}
		}
		
		return result;
	}
	/**
	 * Converts the specified int array to an IPv6 address.
	 * @param array
	 * @return
	 */
	public static Inet6Address intArrayToAddress(int[][] array) {
		String addressWord = "";
		int addedChars = -1;
		int addedColons = 0;
		
		for(int x = 0; x < 6; ++x) {
			for(int y = 0; y < 6; ++y) {
				addedChars++;
				if(addedChars == 4 && addedColons < 7) {
					addressWord += ':';
					addedChars = 0;
					addedColons++;
				}
				addressWord += (char)array[x][y];
			}
		}
		
		//Parsing the address
		addressWord = addressWord.trim();
		Inet6Address address = null;
		try {
			address = (Inet6Address) Inet6Address.getByName(addressWord);
		} catch (UnknownHostException e) {
			System.out.println("Problem with parsing address: " + addressWord + "...");
			e.printStackTrace();
			try {
				Thread.sleep(500);
			} catch (InterruptedException e1) {}
		}
		
		return address;
	}
	
	/**
	 * Converts an array of chars to an image.
	 * @param pxls
	 * @param path
	 */
	public static BufferedImage intArrayToImg(int[][] pxls){		
		final int height = pxls.length;
		final int width = pxls[0].length;
		final BufferedImage image =
		    new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		for (int x = 0; x < height; ++x) {
		    for (int y = 0; y < width; ++y) {
		        image.setRGB(x, y, pxls[x][y] * 10000);
		        //System.out.println("[" + x + "," + y + "] " +  (pxls[x][y] * 10000));
		    }
		}
		
		return image;
	}
	/**
	 * Converts an image to char int array.
	 * Only accepts 6x6 images.
	 * @param image
	 * @return
	 */
	public static int[][] imgToIntArray(BufferedImage image) {
		int[][] result = new int[6][6];
		
		for (int x = 0; x < result.length; ++x) {
		    for (int y = 0; y < result[0].length; ++y) {
		        result[x][y] = (image.getRGB(x, y) + 16777216) / 10000;
		        System.out.println("[" + x + "," + y + "] " +  (char)result[x][y]);
		    }
		}
		
		return result;
	}
	
	/**
	 * Saves the current result as an image.
	 * @param image
	 * @return
	 */
	public static boolean saveResult(String name, BufferedImage image) {
		//Checking if the directory exists.
		File dir = new File("./results");
		if(!dir.exists()) {
			dir.mkdirs();
		}
		
		//Saving the image.
		name = name.replaceAll(":", "-");
		File outputFile = new File("./results/" + name + ".png");
		File outputFileLarge = new File("./results/" + name + "_large.png");
		try {
			if(!ImageIO.write(image, "png", outputFile)) return false;
			if(!ImageIO.write(createResizedCopy(image, 1000, 1000), "png", outputFileLarge)) return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * Scales an image.
	 * @param originalImage
	 * @param scaledWidth
	 * @param scaledHeight
	 * @param preserveAlpha
	 * @return
	 */
	private static BufferedImage createResizedCopy(BufferedImage originalImage, int scaledWidth, int scaledHeight){
        System.out.println("Resizing the generated image...");
        BufferedImage scaledBI = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = scaledBI.createGraphics();
        
        g.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null); 
        g.dispose();
        return scaledBI;
    }
}
