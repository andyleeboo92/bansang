package org.bansang.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileLockInterruptionException;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.bansang.mapper.StoreMapper;
import org.imgscalr.Scalr;
import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import com.gargoylesoftware.htmlunit.javascript.host.html.Option;
import lombok.extern.java.Log;

import lombok.extern.java.Log;

@Log
public class Crolling extends Thread {

	public static WebDriver driver;
	int index;
	String storeName;
	StoreMapper storeMapper;
	Long storeNumber;

	public Crolling(int index, String storeName, StoreMapper storeMapper, Long storeNumber) {
		this.index = index;
		this.storeName = storeName;
		this.storeMapper = storeMapper;
		this.storeNumber = storeNumber;
	}

	public void run() {
		try {
			System.setProperty("webdriver.chrome.driver", "C:\\zzz\\chromedriver\\chromedriver.exe"); // ũ�ҵ���̹� ����
			ChromeOptions options = new ChromeOptions();
			options.addArguments("headless"); // Chrome â �ȶ߰� ĸ��.

			String url = "https://search.naver.com/search.naver?where=image&sm=tab_jum&query=" + storeName;

			driver = new ChromeDriver(options);
			driver.get(url);

			String imgUrl = driver.findElement(By.cssSelector(
					"#_sau_imageTab > div.photowall._photoGridWrapper > div.photo_grid._box > div:nth-child(" + index
							+ ") > a.thumb._thumb > img"))
					.getAttribute("src");

			URL image = new URL(imgUrl);
			BufferedImage img = ImageIO.read(image);
			UUID uuid = UUID.randomUUID();
			String uploadName = uuid.toString();
			String filePath = "C:\\zzz\\crolling\\" + uploadName + ".png";
			// ImageIO.write(img, "png", new File(filePath));

			File file = new File(filePath);
			ImageIO.write(img, "png", new File(filePath));

			BufferedImage origin = ImageIO.read(((MultipartFile) file).getInputStream());
			int imgwidth = Math.min(origin.getHeight(), origin.getWidth());
			int imgheight = imgwidth;
			
			BufferedImage scaledImage = Scalr.crop(origin, (origin.getWidth() - imgwidth)/2, (origin.getHeight() - imgheight)/2, imgwidth, imgheight);

			BufferedImage destImg = Scalr.resize(scaledImage, 400, 400);

			String thumbnailName = "s_" + uploadName;
			
			ImageIO.write(destImg, "png", new FileOutputStream("C:\\zzz\\crolling\\" + thumbnailName));
			
			storeMapper.uploadStoreImage(uploadName, storeNumber);

		} catch (Exception e) {
			e.printStackTrace();
		}

		driver.quit();

	}

}
