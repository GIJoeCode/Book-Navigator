package com.AutoReader.Auto;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class BookNavigator {
	private static final int CHUNK_SIZE = 8500; // Number of words per chunk

	public static void main(String[] args) {
		System.setProperty("webdriver.chrome.driver", "C:\\chromedriver-win64\\chromedriver.exe");
		ChromeOptions options = new ChromeOptions();
		options.setAcceptInsecureCerts(true);

		WebDriver driver = new ChromeDriver(options);
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));

		try {
			navigateSite(driver, wait);
		} finally {
			driver.quit();
		}
	}

	private static void navigateSite(WebDriver driver, WebDriverWait wait) {
		driver.get("https://www.gutenberg.org/ebooks/search/?sort_order=downloads");
		takeScreenshot(driver, "before_finding_books.png");

		try {
			// Select the "Romeo and Juliet" book link
			WebElement bookLink = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a[href='/ebooks/1513']")));
			bookLink.click();

			try {
				// Find the "Read online (web)" link in the table and click it
				WebElement readOnlineLink = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a[href='/ebooks/1513.html.images'][title='Read online']")));
				readOnlineLink.click();

				// Wait for the book content to load and scrape the text
				WebElement bookContent = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("body")));
				String bookText = bookContent.getText();

				// Save the book content to a file
				Files.write(Paths.get("book_content.txt"), bookText.getBytes());
				System.out.println("Book content saved to book_content.txt");

				// Summarize the book content
				summarizeBookContent(bookText);

			} catch (TimeoutException e) {
				System.out.println("Failed to find 'Read online (web)' link within the timeout period.");
				takeScreenshot(driver, "after_timeout_read_online.png");
			}

		} catch (TimeoutException e) {
			System.out.println("Failed to find 'Romeo and Juliet' book link within the timeout period.");
			takeScreenshot(driver, "after_timeout_books.png");
			throw new RuntimeException(e);
		} catch (IOException e) {
			System.out.println("Failed to write book content to file.");
			e.printStackTrace();
		}
	}

	private static void summarizeBookContent(String content) {
		List<String> chunks = splitIntoChunks(content, CHUNK_SIZE);
		List<String> summaries = new ArrayList<>();

		for (String chunk : chunks) {
			try {
				String summary = ChatGPTSummarizer.summarize(chunk);
				summaries.add(summary);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		String finalSummary = String.join(" ", summaries);
		System.out.println("Final Summary:");
		System.out.println(finalSummary);

		try {
			Files.write(Paths.get("summary.txt"), finalSummary.getBytes());
			System.out.println("Summary saved to summary.txt");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static List<String> splitIntoChunks(String text, int chunkSizeWords) {
		List<String> chunks = new ArrayList<>();
		String[] words = text.split("\\s+");
		int length = words.length;
		for (int i = 0; i < length; i += chunkSizeWords) {
			StringBuilder chunk = new StringBuilder();
			for (int j = i; j < Math.min(length, i + chunkSizeWords); j++) {
				chunk.append(words[j]).append(" ");
			}
			chunks.add(chunk.toString().trim());
		}
		return chunks;
	}

	// Utility method to take screenshots
	private static void takeScreenshot(WebDriver driver, String fileName) {
		TakesScreenshot ts = (TakesScreenshot) driver;
		File source = ts.getScreenshotAs(OutputType.FILE);
		String timestamp = String.valueOf(System.currentTimeMillis());
		try {
			Files.copy(source.toPath(), Paths.get(fileName.replace(".png", "_" + timestamp + ".png")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
